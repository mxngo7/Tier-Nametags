package me.mxngo.ui.screens;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.mojang.authlib.GameProfile;

import me.mxngo.TierNametags;
import me.mxngo.config.TierNametagsConfig;
import me.mxngo.mixin.IMinecraftClientAccessor;
import me.mxngo.mixin.IPlayerSkinWidgetAccessor;
import me.mxngo.mixin.IUserCacheInvoker;
import me.mxngo.ocetiers.Gamemode;
import me.mxngo.ocetiers.SkinCache;
import me.mxngo.ui.util.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ProfileScreen extends Screen implements ITierNametagsScreen {
	public static boolean renderWidget = true;
	
	public MinecraftClient mc = MinecraftClient.getInstance();
	private TierNametags instance = TierNametags.getInstance();
	
	private String playerName;
	private boolean shouldCache = false;
	private boolean showLeaderboardButton;
	private Gamemode leaderboardGamemode = null;
	private double leaderboardScroll = 0.0;
	private String leaderboardSearchQuery = "";
	private boolean mouseDown = false;
	private PlayerSkinWidget playerSkinWidget = null;
	
	public AbstractClientPlayerEntity playerEntity;
	private boolean playerAnimationPaused = false;

	private CompletableFuture<Supplier<SkinTextures>> skinTextureSupplier = null;
	
	private float deltaSinceLastRenderModeToggle = 5.0f;
	private float deltaSinceLastSkinRefresh = 60.0f;
//	private float deltaSinceLastAnimationPause = 5.0f;
	private float animationDelta = 0.0f;
	
    public ProfileScreen(String playerName, boolean showLeaderboardButton, Gamemode leaderboardGamemode, double leaderboardScroll, String leaderboardSearchQuery) {
        super(Text.literal("Profile of " + playerName));
        this.playerName = playerName;
        this.showLeaderboardButton = showLeaderboardButton;
        this.leaderboardGamemode = leaderboardGamemode;
        this.leaderboardScroll = leaderboardScroll;
        this.leaderboardSearchQuery = leaderboardSearchQuery;
        this.playerEntity = new AbstractClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), playerName)) {
         	public SkinTextures getSkinTextures() {
         		SkinTextures textures = SkinCache.getPlayer(playerName);
         		if (textures != null) return textures;
         		return SkinCache.getUnknownSkinTextures();
         	};
         	
         	@Override
         	public boolean shouldRenderName() {
         		return false;
         	}
         };
    }
    
    public ProfileScreen(String playerName) {
        super(Text.literal("Profile of " + playerName));
        this.playerName = playerName;
        this.showLeaderboardButton = false;
        this.playerEntity = new AbstractClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), playerName)) {
         	public SkinTextures getSkinTextures() {
         		SkinTextures textures = SkinCache.getPlayer(playerName);
         		if (textures != null) return textures;
         		return SkinCache.getUnknownSkinTextures();
         	};
         	
         	@Override
         	public boolean shouldRenderName() {
         		return false;
         	}
        };
    }
    
    public String getPlayerName() {
    	return this.playerName;
    }
    
    public boolean shouldShowLeaderboardButton() {
    	return this.showLeaderboardButton;
    }
    
    public Gamemode getLeaderboardGamemode() {
    	return this.leaderboardGamemode;
    }
    
    public double getLeaderboardScroll() {
		return this.leaderboardScroll;
	}
    
    public String getLeaderboardSearchQuery() {
		return this.leaderboardSearchQuery;
	}
    
    public boolean isMouseDown() {
    	return this.mouseDown;
    }
    
    public boolean isPlayerAnimationPaused() {
    	return this.playerAnimationPaused;
    }
    
    public float getAnimationDelta() {
    	return this.animationDelta;
    }
    
    public TextRenderer getTextRenderer() {
		return this.textRenderer;
	}
    
    private void fetchSkinTextureSupplier() {
    	ApiServices services = ApiServices.create(((IMinecraftClientAccessor) mc).getAuthenticationService(), mc.runDirectory);
    	
    	CompletableFuture<GameProfile> playerGameProfile = CompletableFuture.supplyAsync(() -> {
    		GameProfile profile;
    		
			Optional<GameProfile> profileQuery = IUserCacheInvoker.findProfileByNameInvoker(services.profileRepository(), playerName);
			if (profileQuery.isPresent()) {
				profile = services.sessionService().fetchProfile(profileQuery.get().getId(), true).profile();
				SkinCache.cacheProfile(profile);
				shouldCache = true;
			} else profile = new GameProfile(UUID.randomUUID(), playerName);

    		return profile;
    	});
    	
    	this.skinTextureSupplier = playerGameProfile.thenApplyAsync(profile -> mc.getSkinProvider().getSkinTexturesSupplier(profile), mc);
    }
    
    @Override
    protected void init() {
    	super.init();
    	
    	if (SkinCache.getPlayer(playerName) != null) {
    		Supplier<SkinTextures> textureSupplier = SkinCache.getSupplier(playerName);
    		
    		if (textureSupplier != null) {
    			int x = RenderUtils.getScaled(50, RenderUtils.iX, this.width);
    			int w = RenderUtils.getScaled(150, RenderUtils.iX, this.width);
    			int h = RenderUtils.getScaled(300, RenderUtils.iY, this.height);
    			playerSkinWidget = new PlayerSkinWidget(w, h, mc.getEntityModelLoader(), textureSupplier);
    			playerSkinWidget.setPosition(x, this.height / 2 - h / 2);
    		}
    	} else fetchSkinTextureSupplier();
    }
    
    @Override
    public void tick() {
    	super.tick();
    	
    	if (this.skinTextureSupplier != null && this.skinTextureSupplier.isDone() && shouldCache) {
    		this.skinTextureSupplier.thenAccept(skinTextureSupplier -> {
    			SkinCache.cachePlayer(playerName, skinTextureSupplier);
    			
    			int x = RenderUtils.getScaled(50, RenderUtils.iX, this.width);
    			int w = RenderUtils.getScaled(150, RenderUtils.iX, this.width);
    			int h = RenderUtils.getScaled(300, RenderUtils.iY, this.height);
    			
    			playerSkinWidget = new PlayerSkinWidget(w, h, mc.getEntityModelLoader(), skinTextureSupplier);
    			playerSkinWidget.setPosition(x, this.height / 2 - h / 2);
    		});
    		
    		skinTextureSupplier = null;
    	}
    }
    
    private void renderPlayerEntity(DrawContext context, int mouseX, int mouseY) {
		float theta = (float) -Math.toDegrees((float) Math.atan2(mouseX - this.width / 8, this.height / 2));
		playerEntity.setHeadYaw(theta);
		playerEntity.setBodyYaw(theta / 3.5f);
		
		if (this.width <= 700 || this.height <= 400) {        	
			theta = (float) Math.toDegrees((float) Math.atan2(mouseY - this.height / 4, this.width / 8));
		} else {
			theta = (float) Math.toDegrees((float) Math.atan2(mouseY - this.height / 3 + 10, this.width / 8));
		}
		playerEntity.setPitch(theta / 2);
		
		float scale = RenderUtils.getScaled(150f, RenderUtils.iY, this.height);
		
		Quaternionf quaternion = new Quaternionf().rotateAxis((float) Math.toRadians(180), new Vector3f(1, 0, 0));
		InventoryScreen.drawEntity(context, 0, 0, scale, new Vector3f(0.75f, 2.5f, 0f), quaternion, null, playerEntity);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        TierNametagsConfig config = instance.getConfig();
        
        animationDelta += delta;
        
        int xOffset = 10 + (showLeaderboardButton ? (textRenderer.getWidth(Text.literal("Back to Leaderboard")) + 12) : 0);
        
        int deltaUntilAnimationComplete = 30;
    	int distanceToMove = 40;
    	
    	int renderModeButtonAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(animationDelta - 5, 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 4) * distanceToMove);
    	int renderModeButtonOffset = config.reduceMotion ? 0 : -renderModeButtonAnimationDistanceProgress;
    	
    	int refreshSkinButtonAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(animationDelta - 10, 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 4) * distanceToMove);
    	int refreshSkinButtonOffset = config.reduceMotion ? 0 : -refreshSkinButtonAnimationDistanceProgress;
        
        boolean hoveringRenderModeToggleButton = RenderUtils.isMouseHovering(this, mouseX, mouseY, xOffset, 10 + renderModeButtonOffset, xOffset + 20, 30 + renderModeButtonOffset);
        boolean hoveringRefreshSkinButton = RenderUtils.isMouseHovering(this, mouseX, mouseY, xOffset + 22, 10 + refreshSkinButtonOffset, xOffset + 42, 30 + refreshSkinButtonOffset);

        RenderUtils.fill(this, context, xOffset, 10 + renderModeButtonOffset, xOffset + 20, 30 + renderModeButtonOffset, hoveringRenderModeToggleButton ? 0x60000000 : 0x80000000);
        RenderUtils.fill(this, context, xOffset + 22, 10 + refreshSkinButtonOffset, xOffset + 42, 30 + refreshSkinButtonOffset, hoveringRefreshSkinButton ? 0x60000000 : 0x80000000);
        
        RenderUtils.renderTexture(this, context, Identifier.of(TierNametags.MODID, "textures/font/render_mode.png"), xOffset + 2, 12 + renderModeButtonOffset, 16, 16);
        RenderUtils.renderTexture(this, context, Identifier.of(TierNametags.MODID, "textures/font/refresh.png"), xOffset + 24, 12 + refreshSkinButtonOffset, 16, 16);
        
        if (mouseDown && hoveringRenderModeToggleButton && deltaSinceLastRenderModeToggle > 5.0f) {
        	deltaSinceLastRenderModeToggle = 0.0f;
        	renderWidget = !renderWidget;
        } else deltaSinceLastRenderModeToggle += delta;
        
        if (mouseDown && hoveringRefreshSkinButton && deltaSinceLastSkinRefresh > 60.0f) {
        	deltaSinceLastSkinRefresh = 0.0f;
        	
        	shouldCache = true;
        	fetchSkinTextureSupplier();
        } else deltaSinceLastSkinRefresh += delta;
        
//        if (!renderWidget) {
//        	int pauseButtonAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(animationDelta - 15, 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 4) * distanceToMove);
//        	int pauseButtonOffset = config.reduceMotion ? 0 : -pauseButtonAnimationDistanceProgress;
//        	
//        	boolean hoveringPauseAnimationButton = RenderUtils.isMouseHovering(this, mouseX, mouseY, xOffset + 44, 10 + pauseButtonOffset, xOffset + 64, 30 + pauseButtonOffset);
//        	
//        	RenderUtils.fill(this, context, xOffset + 44, 10 + pauseButtonOffset, xOffset + 64, 30 + pauseButtonOffset, hoveringPauseAnimationButton ? 0x60000000 : 0x80000000);
//        	RenderUtils.renderTexture(this, context, Identifier.of(TierNametags.MODID, "textures/font/" + (playerAnimationPaused ? "play" : "pause") + ".png"), xOffset + 46, 12 + pauseButtonOffset, 16, 16);
//        	
//        	if (mouseDown && hoveringPauseAnimationButton && deltaSinceLastAnimationPause > 5.0f) {
//        		playerAnimationPaused = !playerAnimationPaused;
//        		deltaSinceLastAnimationPause = 0.0f;
//        	} else deltaSinceLastAnimationPause += delta;
//        }
        
        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();
        
        boolean exists = players.stream().anyMatch(p -> p.getName().getString().equalsIgnoreCase(this.playerName));
        if (exists) RenderUtils.renderTexture(this, context, Identifier.of(TierNametags.MODID, "textures/font/online_status.png"), 267, 52, 29, 28);
        else RenderUtils.renderTexture(this, context, Identifier.of(TierNametags.MODID, "textures/font/offline_status.png"), 267, 52, 29, 28);
        
         if (renderWidget && playerSkinWidget != null) {        	 
        	 playerSkinWidget.render(context, mouseX, mouseY, delta);
        	 if (!(playerSkinWidget.isHovered() && mouseDown && config.reduceMotion))
        		 ((IPlayerSkinWidgetAccessor) playerSkinWidget).setHorizontalRotation(((IPlayerSkinWidgetAccessor) playerSkinWidget).getHorizontalRotation() + 0.05f);
         } else {
        	 renderPlayerEntity(context, mouseX, mouseY);
         }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    
    @Override
    public void close() {
    	super.close();
    	
    	if (this.showLeaderboardButton) {
    		LeaderboardScreen leaderboardScreen = new LeaderboardScreen();
    		if (this.leaderboardGamemode != null) leaderboardScreen.setSelectedTab(List.of(Gamemode.values()).indexOf(this.leaderboardGamemode) + 1);
    		leaderboardScreen.setScroll(this.leaderboardScroll);
    		leaderboardScreen.setSearchQuery(this.leaderboardSearchQuery);
    		mc.setScreen(leaderboardScreen);
    	}
    	
    	if (skinTextureSupplier != null && !skinTextureSupplier.isDone() && shouldCache) {
    		instance.addUncachedTextureSupplier(playerName, skinTextureSupplier);
    	}
    }
    
    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
    	if (playerSkinWidget != null && playerSkinWidget.isHovered())
    		playerSkinWidget.mouseClicked(mouseX, mouseY, button);
    	
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) this.mouseDown = true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (playerSkinWidget != null)
    		playerSkinWidget.mouseReleased(mouseX, mouseY, button);
		
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) this.mouseDown = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (playerSkinWidget != null && playerSkinWidget.isHovered())
    		playerSkinWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	public enum PlayerProfileRenderMode {
		ENTITY,
		WIDGET;
	}
}
