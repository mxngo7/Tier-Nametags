package me.mxngo;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.mxngo.config.ConfigManager;
import me.mxngo.config.TierNametagsConfig;
import me.mxngo.config.TierPosition;
import me.mxngo.ocetiers.Gamemode;
import me.mxngo.ocetiers.Leaderboard;
import me.mxngo.ocetiers.OceTiersAPIWrapper;
import me.mxngo.ocetiers.SkinCache;
import me.mxngo.ocetiers.Tier;
import me.mxngo.ocetiers.TieredPlayer;
import me.mxngo.ui.screens.LeaderboardScreen;
import me.mxngo.ui.screens.ProfileScreen;
import me.mxngo.ui.screens.SettingsScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.MutableText;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class TierNametags implements ModInitializer {
	public static final String MODID = "tiernametags";
	public static final String VERSION = "1.0.1";
	
	private static TierNametags instance = new TierNametags();
	private final Logger logger = LoggerFactory.getLogger(MODID);
	
	private static MinecraftClient mc;
	
	private final StyleSpriteSource.Font gamemodeIconsSpriteSource = new StyleSpriteSource.Font(Identifier.of(TierNametags.MODID, "icons"));
	
	private TieredPlayer[] players = {};
	private HashMap<String, TieredPlayer> playerMap = new HashMap<>();
	private Leaderboard leaderboard;
	
	private KeyBinding cycleGamemodeKeybinding;
	private KeyBinding cycleGamemodeBackwardsKeybinding;
	private KeyBinding leaderboardKeybinding;
	private KeyBinding settingsKeybinding;
	
	private List<Pair<String, CompletableFuture<Supplier<SkinTextures>>>> uncachedTextures = new ArrayList<>();
	
	@Override
	public void onInitialize() {
		mc = MinecraftClient.getInstance();
		
		OceTiersAPIWrapper.getPlayers().thenAccept(players -> {
			instance.logger.info("Successfully loaded " + players.length + " player profiles.");
			
			instance.players = players;
			for (TieredPlayer player : players) {				
				instance.playerMap.put(player.name(), player);
			}
			
			instance.leaderboard = new Leaderboard(players);
		}).exceptionally(exception -> {
			instance.logger.error("Failed to load players.");
			exception.printStackTrace();
			return null;
		});
		
		KeyBinding.Category keyBindingCategory = new KeyBinding.Category(Identifier.of("tiernametags.localemodid"));
		
		instance.cycleGamemodeKeybinding = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("tiernametags.keybinds.cycle", GLFW.GLFW_KEY_UNKNOWN, keyBindingCategory)
		);
		
		instance.cycleGamemodeBackwardsKeybinding = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("tiernametags.keybinds.cyclebackwards", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, keyBindingCategory)
		);
		
		instance.leaderboardKeybinding = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("tiernametags.keybinds.leaderboard", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, keyBindingCategory)
		);
		
		instance.settingsKeybinding = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("tiernametags.keybinds.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, keyBindingCategory)
		);
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            final LiteralCommandNode<FabricClientCommandSource> tierNametagsCommand = dispatcher.register(
                literal(MODID)
                    .then(literal("profile")
                        .then(argument("player", StringArgumentType.word())
                    		.suggests((ctx, builder) -> {
                                if (mc.world != null) {
                                    for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                                        builder.suggest(player.getName().getString());
                                    }
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "player");
                                FabricClientCommandSource source = ctx.getSource();
                                
                                boolean playerExists = List.of(instance.players).stream().anyMatch(player -> player.name().equalsIgnoreCase(name));
                                if (!playerExists) {
                                	source.sendError(instance.getTierNametagsChatLabel(0xFF5959, 0x9C0909).append(" ").append(name + " does not have an OceTiers profile"));
                                	return 0;
                                }
                                
                                mc.send(() -> {
                                	mc.setScreen(new ProfileScreen(name));
                                });
                                
                                return 1;
                            })
                        )
                    )
                    
	                .then(literal("settings")
	                	.executes(ctx -> {
	                		mc.send(() -> {
	                			mc.setScreen(new SettingsScreen());
	                		});
	                		return 1;
	                	})
	                )
	                
                	.then(literal("gamemode")
	        			.then(literal("set")
        					.then(argument("gamemode", StringArgumentType.word())
                        			.suggests((ctx, builder) -> {
                        				for (Gamemode gamemode : Gamemode.values()) {
                        					builder.suggest(gamemode.getCommandString());
                        				}
                        				return builder.buildFuture();
                        			})
                        			.executes(ctx -> {
                        				String gamemodeName = StringArgumentType.getString(ctx, "gamemode");
                        				FabricClientCommandSource source = ctx.getSource();
                        				
                        				Gamemode gamemode = Gamemode.fromCommandString(gamemodeName);
                        				
                        				if (gamemode == null) {
                        					source.sendError(instance.getTierNametagsChatLabel(0xFF5959, 0x9C0909).append(" ").append("Gamemode " + gamemodeName + " does not exist"));
                        					return 0;
                        				}
                        				
                        				getConfig().tierGamemode = gamemode;
                        				ConfigManager.getInstance().saveConfig();
                        				
                        				instance.sendChatMessage(instance.getCycledGamemodeChatMessage());
                        				return 1;
                        			})
                        		)
            			)
	        			
	        			.then(literal("view")
	        				.then(argument("gamemode", StringArgumentType.word())
                    			.suggests((ctx, builder) -> {
                    				for (Gamemode gamemode : Gamemode.values()) {
                    					builder.suggest(gamemode.getCommandString());
                    				}
                    				return builder.buildFuture();
                    			})
                    			.then(argument("player", StringArgumentType.word())
                					.suggests((ctx, builder) -> {
                						if (mc.world != null) {
                							for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                								builder.suggest(player.getName().getString());
                							}
                						}
                						return builder.buildFuture();
                					})
                					.executes(ctx -> {
                						FabricClientCommandSource source = ctx.getSource();
                						
                						String gamemodeName = StringArgumentType.getString(ctx, "gamemode");
                						String playerName = StringArgumentType.getString(ctx, "player");
                						
                						Gamemode gamemode = Gamemode.fromCommandString(gamemodeName);
                						if (gamemode == null) {
                							source.sendError(instance.getTierNametagsChatLabel(0xFF5959, 0x9C0909).append(" ").append("Gamemode " + gamemodeName + " does not exist"));
                							return 0;
                						}
                						
                						TieredPlayer player = instance.getPlayerCaseInsensitive(playerName);
                						if (player == null) {
                							source.sendError(instance.getTierNametagsChatLabel(0xFF5959, 0x9C0909).append(" ").append(playerName + " does not have an OceTiers profile"));
                							return 0;
                						}
                						
                						Tier tier = player.getTier(gamemode);
                						
                						if (tier == Tier.NONE) {
                							source.sendFeedback(instance.getTierNametagsChatLabel().append(" " + player.name() + " is unranked in " + gamemode.getName()));
                							return 1;
                						}
                						
                						source.sendFeedback(getTierNametagsChatLabel().append(" " + player.name() + "'s " + gamemode.getName() + " tier is ").append(Text.literal(tier.name() + " ").styled(style -> style.withColor(tier.getLightColour()))).append(Text.literal(tier.getIcon()).styled(style -> style.withFont(gamemodeIconsSpriteSource))));
                						
                						return 1;
                					})
            					)
                    		)
	        			)
                	)
                	
                	.then(literal("leaderboard")
                		.executes(ctx -> {
                			mc.send(() -> {
                				mc.setScreen(new LeaderboardScreen());
                			});
                			return 1;
                		})
                	)
                	
                	.then(literal("advanced")
                		.then(literal("clear_skin_cache")
                			.executes(ctx -> {
                				FabricClientCommandSource source = ctx.getSource();
                				
                				SkinCache.clear();
                				SkinCache.cacheWorld(mc.world);
                				
                				source.sendFeedback(getTierNametagsChatLabel().append(" Skin cache successfully cleared"));
                				return 1;
                			})
                		)
                	)
            );
            
            dispatcher.register(literal("tn").redirect(tierNametagsCommand));
        });
	}
	
	public void tick() {
		for (Pair<String, CompletableFuture<Supplier<SkinTextures>>> pair : new ArrayList<>(uncachedTextures)) {
			if (pair.getRight() == null) uncachedTextures.remove(pair);
			if (pair.getRight().isDone()) pair.getRight().thenAccept(skinTextureSupplier -> SkinCache.cachePlayer(pair.getLeft(), skinTextureSupplier));
		}
		
		if (instance.leaderboardKeybinding != null && instance.leaderboardKeybinding.wasPressed()) mc.setScreen(new LeaderboardScreen());
		else if (instance.settingsKeybinding != null && instance.settingsKeybinding.wasPressed()) mc.setScreen(new SettingsScreen());
		
		if (instance.cycleGamemodeKeybinding == null || instance.cycleGamemodeBackwardsKeybinding == null) return;
		
		boolean cycleBackwardsPressed = instance.cycleGamemodeBackwardsKeybinding.wasPressed();
		if (instance.cycleGamemodeKeybinding.wasPressed() || cycleBackwardsPressed) instance.cycleSelectedGamemode(cycleBackwardsPressed);
	}
	
	public void onKey(long window, int key, int scancode, int action, int modifiers) {
		if (mc.currentScreen instanceof LeaderboardScreen) {
			((LeaderboardScreen) mc.currentScreen).onKey(window, key, scancode, action, modifiers);
		}
	}
	
	public Logger getLogger() {
		return instance.logger;
	}
	
	public TieredPlayer[] getPlayers() {
		return instance.players;
	}
	
	public TieredPlayer getPlayer(String name) {
		return instance.playerMap.get(name);
	}
	
	public TieredPlayer getPlayerCaseInsensitive(String name) {
		for (TieredPlayer player : instance.players) {
			if (player.name().equalsIgnoreCase(name)) return player;
		}
		
		return null;
	}
	
	public MutableText getNametagComponent(String name) {
		TieredPlayer player = instance.getPlayer(name);
		if (player == null) return null;
		
		Gamemode gamemode = instance.getSelectedGamemode();
		Tier tier = player.getTier(gamemode);
		if (tier == null || tier.getIcon() == null) return null;
		
		TierNametagsConfig config = getConfig();
		
		MutableText gamemodeIcon = Text.literal(gamemode.getIcon()).styled(style -> style.withFont(gamemodeIconsSpriteSource).withColor(0xFFFFFFFF));
		MutableText tierIcon = Text.literal(tier.getIcon()).styled(style -> style.withFont(gamemodeIconsSpriteSource).withColor(0xFFFFFFFF));
		MutableText tierText = Text.literal(tier.name()).styled(style -> style.withColor(tier.getLightColour()));
		
		if (config.tierPosition == TierPosition.LEFT) {
			if (config.showTierText) return Text.empty().append(gamemodeIcon).append(tierIcon).append(" ").append(tierText).append(" |");
			else return Text.empty().append(gamemodeIcon).append(tierIcon);
		} else {
			if (config.showTierText) return Text.empty().append("| ").append(tierText).append(" ").append(tierIcon).append(gamemodeIcon);
			else return tierIcon.append(gamemodeIcon);
		}
	}
	
	public MutableText applyTierToDisplayName(String name, MutableText displayName, MutableText component) {
		TierNametagsConfig config = getConfig();
		
		String plainText = displayName.getString();
		int index = plainText.indexOf(name);
		
		if (index < 0) {
			return config.tierPosition == TierPosition.LEFT
				? Text.empty().append(component).append(" ").append(displayName)
				: Text.empty().append(displayName).append(" ").append(component);
		} else {
			MutableText result = Text.empty();
			for (Text child : displayName.getWithStyle(displayName.getStyle())) {
				if (child == null) {
					continue;
				}
				
				String content = child.getString();
				
				if (content.contains(name)) {
					if (config.tierPosition == TierPosition.LEFT) result.append(component).append(" ").append(child);
					else result.append(child).append(" ").append(component);
				} else result.append(child);
			}
			
			if (result == null) return displayName;
			else return result;
		}
	}
	
	public Leaderboard getLeaderboard() {
		return instance.leaderboard;
	}
	
	public Gamemode getSelectedGamemode() {
		return getConfig().tierGamemode;
	}
	
	public void cycleSelectedGamemode() {
		TierNametagsConfig config = getConfig();
		
		int index = Arrays.asList(Gamemode.values()).indexOf(config.tierGamemode);
		int length = Gamemode.values().length;
		if ((index + 1) == length) config.tierGamemode = Gamemode.values()[0];
		else config.tierGamemode = Gamemode.values()[index + 1];
		
		ConfigManager.getInstance().saveConfig();
		instance.sendChatMessage(instance.getCycledGamemodeChatMessage());
	}
	
	public void cycleSelectedGamemode(boolean reverse) {
		TierNametagsConfig config = getConfig();
		
		if (!reverse) {
			instance.cycleSelectedGamemode();
		} else {			
			int index = Arrays.asList(Gamemode.values()).indexOf(config.tierGamemode);
			int length = Gamemode.values().length;
			if ((index - 1) == -1) config.tierGamemode = Gamemode.values()[length - 1];
			else config.tierGamemode = Gamemode.values()[index - 1];
			
			ConfigManager.getInstance().saveConfig();
			instance.sendChatMessage(instance.getCycledGamemodeChatMessage());
		}
	}
	
	private MutableText getTierNametagsChatLabel() {
		return (MutableText) Text.literal("[").append(instance.withTextGradient(0xAAFAF4, 0x57ABA5, "TierNametags")).append(Text.literal("]"));
	}
	
	private MutableText getTierNametagsChatLabel(int startColour, int stopColour) {
		return (MutableText) Text.literal("[").append(instance.withTextGradient(startColour, stopColour, "TierNametags")).append(Text.literal("]"));
	}
	
	public void sendChatMessage(Text message) {
		mc.player.sendMessage(instance.getTierNametagsChatLabel().append(" ").append(message), false);
	}
	
	private MutableText getCycledGamemodeChatMessage() {
		TierNametagsConfig config = getConfig();
		return Text.literal("Cycled gamemode to " + config.tierGamemode.getName()).append(" ").append(Text.literal(config.tierGamemode.getIcon()).styled(style -> style.withFont(gamemodeIconsSpriteSource)));
	}
	
	public void addUncachedTextureSupplier(String playerName, CompletableFuture<Supplier<SkinTextures>> textureSupplier) {
		instance.uncachedTextures.add(new Pair<>(playerName, textureSupplier));
	}
	
	public static int[] createGradient(int startColour, int stopColour, int steps) {
		int[] gradient = new int[steps];

        Color c1 = new Color(startColour, (startColour >> 24) != 0);
        Color c2 = new Color(stopColour, (stopColour >> 24) != 0);

        for (int i = 0; i < steps; i++) {
            float t = (float) i / (steps - 1);

            int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
            int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
            int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
            int a = (int) (c1.getAlpha() + t * (c2.getAlpha() - c1.getAlpha()));

            gradient[i] = new Color(r, g, b, a).getRGB();
        }
        
        return gradient;
	}
	
	private MutableText withTextGradient(int startColour, int stopColour, String text) {
		int steps = text.length();
		if (steps < 2) return (MutableText) Text.literal(text);

        int[] gradient = createGradient(startColour, stopColour, steps);
        MutableText textGradient = Text.empty();
        
        int i = 0;
        for (int colour : gradient) {
        	textGradient.append(Text.literal(String.valueOf(text.charAt(i))).styled(style -> style.withColor(colour)));
        	i++;
        }
        
        return textGradient;
	}
	
	public TierNametagsConfig getConfig() {
		return ConfigManager.getInstance().getConfig();
	}
	
	public static TierNametags getInstance() {
		return instance;
	}
}