package me.mxngo.mixin;

import java.awt.Color;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.mxngo.TierNametags;
import me.mxngo.config.TierNametagsConfig;
import me.mxngo.ocetiers.Gamemode;
import me.mxngo.ocetiers.Leaderboard;
import me.mxngo.ocetiers.SkinCache;
import me.mxngo.ocetiers.Tier;
import me.mxngo.ocetiers.TieredPlayer;
import me.mxngo.ui.ProfileTheme;
import me.mxngo.ui.screens.LeaderboardScreen;
import me.mxngo.ui.screens.ProfileScreen;
import me.mxngo.ui.screens.SettingsScreen;
import me.mxngo.ui.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Mixin(Screen.class)
public class ScreenMixin {
	private TierNametags instance = TierNametags.getInstance();
	
	private void renderProfileScreen(ProfileScreen screen, DrawContext context, int mouseX, int mouseY, float delta) {
		if (!screen.isPlayerAnimationPaused()) screen.playerEntity.limbAnimator.updateLimbs(0.35f, 1.0f, 0.035f);
		
		TierNametagsConfig config = instance.getConfig();
		Leaderboard leaderboard = instance.getLeaderboard();
		TieredPlayer player = instance.getPlayerCaseInsensitive(screen.getPlayerName());
		
		ProfileTheme theme = player.getProfileTheme();
		int start = theme.getBackgroundStart();
		int stop = theme.getBackgroundEnd();
		int borderStart = theme.getBorderStart();
		int borderStop = theme.getBorderEnd();
		
        RenderUtils.fillGradient(screen, context, 0, 0, RenderUtils.iX, RenderUtils.iY, start, stop);
        
        RenderUtils.fill(screen, context, 250, 25, RenderUtils.iX - 50, 100, 0x40000000);
        RenderUtils.fill(screen, context, 250, 100, RenderUtils.iX - 50, RenderUtils.iY - 25, 0x80000000);
        RenderUtils.renderGradientBorder(screen, context, 250, 25, RenderUtils.iX - 300, RenderUtils.iY - 50, borderStart, borderStop, 3);
        
        int deltaUntilAnimationComplete = 30;
    	int distanceToMove = 40;
        
        if (screen.shouldShowLeaderboardButton()) {
        	int backButtonAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getAnimationDelta(), 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 4) * distanceToMove);
        	int backButtonOffset = config.reduceMotion ? 0 : -backButtonAnimationDistanceProgress;
        	
        	Text backText = Text.literal("Back to Leaderboard");
        	boolean hoveringBackButton = RenderUtils.isMouseHovering(screen, mouseX, mouseY, 10, 10 + backButtonOffset, screen.getTextRenderer().getWidth(backText) + 20, 30);
        	
        	RenderUtils.fill(screen, context, 10, 10 + backButtonOffset, screen.getTextRenderer().getWidth(backText) + 20, 30 + backButtonOffset, hoveringBackButton ? 0x60000000 : 0x80000000);
        	RenderUtils.renderScaledText(screen, context, backText, 15, 20 - screen.getTextRenderer().fontHeight / 2 + backButtonOffset, 0xFFFFFFFF, 1.0f);
        	
        	if (hoveringBackButton && screen.isMouseDown()) {
        		LeaderboardScreen leaderboardScreen = new LeaderboardScreen();
        		if (screen.getLeaderboardGamemode() != null) leaderboardScreen.setSelectedTab(List.of(Gamemode.values()).indexOf(screen.getLeaderboardGamemode()) + 1);
        		leaderboardScreen.setScroll(screen.getLeaderboardScroll());
        		leaderboardScreen.setSearchQuery(screen.getLeaderboardSearchQuery());
        		MinecraftClient.getInstance().setScreen(leaderboardScreen);
        	}
        }
        
        distanceToMove = 30;
        deltaUntilAnimationComplete = 50;
        int nameTextAnimationOffset = config.reduceMotion ? 0 : distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getAnimationDelta(), 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 4) * distanceToMove);
        
        Text playerNameAndRankingText = Text.literal(player.name()).append(" (").append(Text.literal("#" + String.valueOf(player.getLeaderboardPosition())).withColor(instance.getLeaderboard().getForegroundColour(player.getLeaderboardPosition()))).append(")");
        RenderUtils.renderScaledText(screen, context, playerNameAndRankingText, 305 + nameTextAnimationOffset, 57 - screen.getTextRenderer().fontHeight / 4, 0xFFFFFFFF, 3.0f);
        
        distanceToMove = 15;
        deltaUntilAnimationComplete = 50;
        int ptsTextAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getAnimationDelta(), 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 4) * distanceToMove);
    	int ptsTextAnimationOffset = config.reduceMotion ? 0 : -ptsTextAnimationDistanceProgress;
    	
        Text pointsText = Text.literal(String.valueOf(player.getLeaderboardPoints())).append(" pts");
        RenderUtils.renderScaledText(screen, context, pointsText, RenderUtils.iX - 50 - ((int) (screen.getTextRenderer().getWidth(pointsText) * 3)) - 20 + ptsTextAnimationOffset, 57 - screen.getTextRenderer().fontHeight / 4, player.getLeaderboardPosition() > 1 ? new Color(leaderboard.getForegroundColour(player.getLeaderboardPosition())).brighter().getRGB() | (0xFF << 24) : leaderboard.getForegroundColour(player.getLeaderboardPosition()), 3.0f);
        
        // Would have liked this implemented but the OceTiers API /leaderboard endpoint doesn't include 'last_updated' for some reason.
        // Can implement with /profile endpoint but would need a request for each player. Also the endpoint follows the format /players/{id}/profile
        // where the id is the player's discord id. Like really?
//        distanceToMove = 10;
//        deltaUntilAnimationComplete = 35;
//        
//        int lastUpdatedTextAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getAnimationDelta() - 2, 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 4) * distanceToMove);
//    	  int lastUpdatedTextAnimationOffset = config.reduceMotion ? 0 : -lastUpdatedTextAnimationDistanceProgress;
//    	
//        Text lastUpdatedText = Text.literal("Last updated " + player.getDaysSinceLastUpdate() + " day" + (player.getDaysSinceLastUpdate() == 1 ? "" : "s") + " ago");
//        RenderUtils.renderScaledText(screen, context, lastUpdatedText, RenderUtils.iX - 50 - screen.getTextRenderer().getWidth(lastUpdatedText) - 20 + lastUpdatedTextAnimationOffset, 85 - screen.getTextRenderer().fontHeight / 2, 0xFFe9e9e9, 1.0f);
        
        int cardXOffset = 250;
        int cardYOffset = 100;
        int cardWidth = (RenderUtils.iX - 360) / 2;
        int cardHeight = 47;
        
        int index = 0;
        for (Gamemode gamemode : Gamemode.values()) {
        	Tier tier = player.getTier(gamemode);
        	
        	distanceToMove = 7;
        	deltaUntilAnimationComplete = 20;
        	int cardAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getAnimationDelta() - 2 * (index > 4 ? index - 5 : index), 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 3) * distanceToMove);
        	int cardAnimationOffset = config.reduceMotion ? 0 : ((index > 4 ? -1 : 1) * cardAnimationDistanceProgress);
        	
        	boolean isHoveringGamemodeCard = RenderUtils.isMouseHovering(screen, mouseX, mouseY, 20 + cardXOffset, 20 + cardYOffset, cardXOffset + cardWidth - 30, 20 + cardYOffset + cardHeight);
        	boolean isHoveringTierCard = RenderUtils.isMouseHovering(screen, mouseX, mouseY, cardXOffset + cardWidth - 30, 20 + cardYOffset, 20 + cardXOffset + cardWidth, 20 + cardYOffset + cardHeight);
        	
        	if (isHoveringGamemodeCard || isHoveringTierCard) {
        		RenderUtils.fill(screen, context, 20 + cardXOffset - 5 + cardAnimationOffset, 20 + cardYOffset - 2, cardXOffset + cardWidth - 30 + cardAnimationOffset, 20 + cardYOffset + cardHeight + 2, isHoveringGamemodeCard ? 0x30FFFFFF : 0x20FFFFFF);
        		RenderUtils.fill(screen, context, cardXOffset + cardWidth - 30 + cardAnimationOffset, 20 + cardYOffset - 2, 20 + cardXOffset + cardWidth + 5 + cardAnimationOffset, 20 + cardYOffset + cardHeight + 2, isHoveringTierCard ? 0x60FFFFFF : 0x40FFFFFF);
        		RenderUtils.renderBorder(screen, context, 20 + cardXOffset - 5 + cardAnimationOffset, 20 + cardYOffset - 2, cardWidth + 10, cardHeight + 4, 0x80FFFFFF);
        	} else {
        		RenderUtils.fill(screen, context, 20 + cardXOffset + cardAnimationOffset, 20 + cardYOffset, cardXOffset + cardWidth - 30 + cardAnimationOffset, 20 + cardYOffset + cardHeight, isHoveringGamemodeCard ? 0x30FFFFFF : 0x20FFFFFF);
        		RenderUtils.fill(screen, context, cardXOffset + cardWidth - 30 + cardAnimationOffset, 20 + cardYOffset, 20 + cardXOffset + cardWidth + cardAnimationOffset, 20 + cardYOffset + cardHeight, isHoveringTierCard ? 0x60FFFFFF : 0x40FFFFFF);
        		RenderUtils.renderBorder(screen, context, 20 + cardXOffset + cardAnimationOffset, 20 + cardYOffset, cardWidth, cardHeight, 0x80FFFFFF);
        	}
        	
        	Text gamemodeText = Text.literal(gamemode.getName());
        	RenderUtils.renderScaledText(screen, context, gamemodeText, 75 + cardXOffset + cardAnimationOffset, cardYOffset + cardHeight / 2 + 7, 0xFFFFFFFF, 1.8f);
        	
        	Text tierText = (tier == Tier.NONE) ? Text.literal("Unranked") : Text.literal(tier.name());
        	RenderUtils.renderScaledTextWithGradient(screen, context, tierText, 75 + cardXOffset + cardAnimationOffset, cardYOffset + cardHeight + 2, tier.getLightColour(), tier.getDarkColour(), 1.2f);
        	
        	List<TieredPlayer> gamemodePlayers = leaderboard.getPlayers(gamemode);
        	if (gamemodePlayers.contains(player)) {
        		int rank = gamemodePlayers.indexOf(player) + 1;
    			Text rankText = Text.literal("(").append(Text.literal("#" + rank).withColor(leaderboard.getForegroundColour(rank))).append(")");
    			RenderUtils.renderScaledText(screen, context, rankText, 80 + cardXOffset + (int) (screen.getTextRenderer().getWidth(tierText) * 1.2) + cardAnimationOffset, cardYOffset + cardHeight + 2, 0xFFFFFFFF, 1.2f);
        	}
        	
        	if (gamemode.getIconPath() != null) {
        		int size = isHoveringGamemodeCard ? 36 : 32;
        		RenderUtils.renderTexture(screen, context, Identifier.of(TierNametags.MODID, gamemode.getIconPath()), cardXOffset + 28 - (isHoveringGamemodeCard ? 2 : 0) + cardAnimationOffset, cardYOffset + 4 + cardHeight / 2 - (isHoveringGamemodeCard ? 2 : 0), size, size);
        	}
        	
        	if (tier.getIconPath() != null) {
        		int size = isHoveringTierCard ? 44 : 40;
        		RenderUtils.renderTexture(screen, context, Identifier.of(TierNametags.MODID, tier.getIconPath()), cardXOffset + cardWidth - size / 2 - 4 + ((isHoveringGamemodeCard || isHoveringTierCard) ? 2 : 0) + cardAnimationOffset, cardYOffset + cardHeight / 2 - (isHoveringTierCard ? 2 : 0), size, size);
        	}
        	
        	if (tier != Tier.NONE && (isHoveringGamemodeCard || isHoveringTierCard) && screen.isMouseDown() && screen.getAnimationDelta() > 5.0f) {
        		LeaderboardScreen leaderboardScreen = new LeaderboardScreen();
        		leaderboardScreen.setSelectedTab(index + 1);
        		leaderboardScreen.setPlayerToFind(player.name());
        		leaderboardScreen.setDeltaSinceFoundOwnPlayerSearch(0f);
        		screen.mc.setScreen(leaderboardScreen);
        	}
        	
        	cardYOffset += cardHeight + 20;
        	index++;
        	
        	if (index % 5 == 0) {
        		cardXOffset += cardWidth + 20;
        		cardYOffset = 100;
        	}
        }
	}
	
	private void renderLeaderboard(LeaderboardScreen screen, DrawContext context, double mouseX, double mouseY, float delta, Gamemode gamemode, List<TieredPlayer> players) {
	    if (players == null || players.isEmpty()) return;
	    
	    TierNametagsConfig config = instance.getConfig();
	    
	    int cardWidth = 580;
	    int cardHeight = 30;
	    int cardXOffset = 250;
	    int cardInitialY = 50;
	    int paddingX = 2;
	    int paddingY = 8;
	    
	    int cardColour = 0x80000000;
	    int cardHoverColour = 0x60000000;
	    int rankingColour = 0xAA000000;
	    
	    int visibleRows = (screen.height - cardInitialY) / (cardHeight + paddingY);
	    
	    int s = 0;
	    for (int i = 0; i < players.size(); i++) {
	    	TieredPlayer player = players.get(i);
	    	if (!player.name().toLowerCase().contains(screen.getSearchQuery().toLowerCase())) {
	    		s++;
	    		continue;
	    	}
	    }
	    
	    int totalRows = players.size() - s;
	    
	    double maxScroll = Math.max(0, totalRows - visibleRows);
	    screen.setScroll(Math.max(0, Math.min(screen.getScroll(), maxScroll)));
	    
	    double scrollOffset = screen.getScroll() * (cardHeight + paddingY);
	    
	    int deltaUntilAnimationComplete = 35;
    	int distanceToMove = 50;
    	
    	int searchAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getSidebarEntriesAnimationDelta(), 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 3) * distanceToMove);
    	int searchAnimationOffset = config.reduceMotion ? 0 : -searchAnimationDistanceProgress;
    	
    	int clearInputAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getSidebarEntriesAnimationDelta() - 6, 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 3) * distanceToMove);
    	int clearInputAnimationOffset = config.reduceMotion ? 0 : -clearInputAnimationDistanceProgress;
    	
    	int findMeAnimationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getSidebarEntriesAnimationDelta() - 12, 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 3) * distanceToMove);
    	int findMeAnimationOffset = config.reduceMotion ? 0 : -findMeAnimationDistanceProgress;
    	
    	int searchButtonWidth = 90;
    	int findMeButtonWidth = 70;
    	boolean hoveringSearchInput = RenderUtils.isMouseHovering(screen, mouseX, mouseY, cardXOffset, (int) (10 - scrollOffset) + searchAnimationOffset, cardXOffset + cardWidth - findMeButtonWidth - searchButtonWidth - paddingX, (int) (10 + cardHeight - scrollOffset) + searchAnimationOffset);
    	boolean hoveringClearSearchButton = RenderUtils.isMouseHovering(screen, mouseX, mouseY, cardXOffset + cardWidth - findMeButtonWidth - searchButtonWidth, 10 + clearInputAnimationOffset, cardXOffset + cardWidth - findMeButtonWidth, 10 + cardHeight + clearInputAnimationOffset);
    	boolean hoveringFindMeButton = RenderUtils.isMouseHovering(screen, mouseX, mouseY, cardXOffset + cardWidth - findMeButtonWidth + paddingX, (int) (10 - scrollOffset) + findMeAnimationOffset, cardXOffset + cardWidth, (int) (10 + cardHeight - scrollOffset) + findMeAnimationOffset);
    	
	    RenderUtils.fill(screen, context, cardXOffset, (int) (10 - scrollOffset) + searchAnimationOffset, cardXOffset + cardWidth - findMeButtonWidth - searchButtonWidth - paddingX, (int) (10 + cardHeight - scrollOffset) + searchAnimationOffset, hoveringSearchInput ? cardHoverColour : cardColour);
	    
	    if (screen.isSearching()) RenderUtils.renderBorder(screen, context, cardXOffset, (int) (10 - scrollOffset) + searchAnimationOffset, cardWidth - findMeButtonWidth - searchButtonWidth - paddingX, cardHeight, 0xFFFFFFFF);
	    else if (!screen.isSearching() && screen.getSearchQuery().isEmpty()) RenderUtils.renderScaledText(screen, context, Text.literal("Search..."), cardXOffset + 10, (int) (10 - scrollOffset) + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2 + searchAnimationOffset, 0xFFFFFFFF, 1.0f);

	    RenderUtils.fill(screen, context, cardXOffset + cardWidth - findMeButtonWidth - searchButtonWidth, (int) (10 - scrollOffset) + clearInputAnimationOffset, cardXOffset + cardWidth - findMeButtonWidth, (int) (10 + cardHeight - scrollOffset) + clearInputAnimationOffset, hoveringClearSearchButton ? cardHoverColour : cardColour);
	    RenderUtils.fill(screen, context, cardXOffset + cardWidth - findMeButtonWidth + paddingX, (int) (10 - scrollOffset) + findMeAnimationOffset, cardXOffset + cardWidth, (int) (10 + cardHeight - scrollOffset) + findMeAnimationOffset, hoveringFindMeButton ? cardHoverColour : cardColour);
	    
	    Text searchText = Text.literal("Clear Search");
	    RenderUtils.renderScaledText(screen, context, searchText, cardXOffset + cardWidth - findMeButtonWidth - searchButtonWidth / 2 - screen.getTextRenderer().getWidth(searchText) / 2, (int) (10 + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2 - scrollOffset) + clearInputAnimationOffset, 0xFFFFFFFF, 1.0f);
	    
	    if (hoveringClearSearchButton && screen.isMouseDown() && !screen.isDraggingScrollbar()) {
	    	screen.setSearchQuery("");
	    }
	    
	    if (hoveringSearchInput && !screen.isDraggingScrollbar() && screen.isMouseDown()) {
	    	screen.toggleSearching();
	    } else if (!hoveringSearchInput && screen.isMouseDown()) {
	    	screen.setSearching(false);
	    }
    	else {
    		screen.setDeltaSinceToggledSearching(screen.getDeltaSinceToggledSearching() + delta);
	    }
	    
	    if (!screen.getSearchQuery().isEmpty()) {
	    	RenderUtils.renderScaledText(screen, context, Text.literal(screen.getSearchQuery()), cardXOffset + 10, (int) (10 - scrollOffset) + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2 + searchAnimationOffset, 0xFFFFFFFF, 1.0f);
	    	
	    	String autocompletion = screen.getAutocompletion(screen.getSearchQuery(), players);
	    	if (!autocompletion.isEmpty()) {
	    		RenderUtils.renderScaledText(screen, context, Text.literal(autocompletion), cardXOffset + 10 + screen.getTextRenderer().getWidth(screen.getSearchQuery()), (int) (10 - scrollOffset) + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2 + searchAnimationOffset, 0xFFc9c9c9, 1.0f);
	    	}
	    }
	    
	    Text findMeText = Text.literal("Go To Me");
	    RenderUtils.renderScaledText(screen, context, findMeText, cardXOffset + cardWidth - findMeButtonWidth / 2 - screen.getTextRenderer().getWidth(findMeText) / 2, (int) (10 + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2 - scrollOffset) + findMeAnimationOffset, 0xFFFFFFFF, 1.0f);
	    
	    String name = screen.mc.player.getGameProfile().getName();
	    
    	if (!screen.getPlayerToFind().isEmpty()) {
	    	for (int i = 0; i < players.size(); i++) {
	    		TieredPlayer player = players.get(i);
	    		if (player.name().equalsIgnoreCase(screen.getPlayerToFind())) {
	    			screen.setScroll((cardInitialY + i * (cardHeight + paddingY)) / (cardHeight + paddingY));
	    		}
	    	}
	    }
    	
    	if ((hoveringFindMeButton && !screen.isDraggingScrollbar() && screen.isMouseDown())) {
    		screen.setSearching(false);
    		screen.setSearchQuery("");
    		
    		for (int i = 0; i < players.size(); i++) {
    			TieredPlayer player = players.get(i);
    			if (player.name().equalsIgnoreCase(name)) {
    				screen.setScroll((cardInitialY + i * (cardHeight + paddingY)) / (cardHeight + paddingY));
    			}
    		}
    		
    		screen.setDeltaSinceFoundOwnPlayerSearch(0.0f);
    	} else screen.setDeltaSinceFoundOwnPlayerSearch(screen.getDeltaSinceFoundOwnPlayerSearch() + delta);
	    
	    int skipped = 0;
	    int invisible = 0;

	    for (int index = 0; index < players.size(); index++) {
	    	int y = (int) (cardInitialY + (index - skipped) * (cardHeight + paddingY) - scrollOffset);
	    	if (RenderUtils.getScaled(y + cardHeight, RenderUtils.iY, screen.height) < 0 || RenderUtils.getScaled(y, RenderUtils.iY, screen.height) > screen.height) {
	    		invisible++;
	    		continue;
	    	}

	    	Leaderboard leaderboard = instance.getLeaderboard();
	    	rankingColour = leaderboard.getBackgroundColour(index + 1);

	    	TieredPlayer player = players.get(index);
	    	if (!player.name().toLowerCase().contains(screen.getSearchQuery().toLowerCase())) {
	    		skipped++;
	    		continue;
	    	}
	    	
	    	Text leaderboardPositionText = Text.literal("#" + (index + 1));
	    	int rankingWidth = screen.getTextRenderer().getWidth(Text.empty().append(leaderboardPositionText).append((index < 9 ? "0" : ""))) + 15;
	    	
	    	boolean shouldAnimate = index < 150 + invisible;
	    	deltaUntilAnimationComplete = 40;
	    	distanceToMove = cardWidth;
	    	int animationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getLeaderboardEntriesAnimationDelta() - (index - invisible) / 2f, 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 4) * distanceToMove);
	    	int animationOffset = config.reduceMotion ? 0 : ((shouldAnimate && screen.getSearchQuery().isEmpty()) ? animationDistanceProgress : 0);
	    	
	    	boolean isHovered = RenderUtils.isMouseHovering(screen, mouseX, mouseY, cardXOffset + animationOffset, y, cardXOffset + cardWidth, y + cardHeight);
	    	
	    	if ((player.name().equalsIgnoreCase(name) && screen.getDeltaSinceFoundOwnPlayerSearch() < 20f) || screen.getPlayerToFind().equalsIgnoreCase(player.name())) {
	    		float fade = Math.min(screen.getDeltaSinceFoundOwnPlayerSearch() / 20f, 1.0f);
	    		int a1 = Math.round(((rankingColour >> 24) & 0xFF) * fade);
	    		int a2 = Math.round(((cardColour >> 24) & 0xFF) * fade);
	    		int a3 = Math.round(((leaderboard.getTextColour(index + 1) >> 24) & 0xFF) * fade);
	    		int c1 = (a1 << 24) | (rankingColour & 0x00FFFFFF);
	    		int c2 = (a2 << 24) | (cardColour & 0x00FFFFFF);
	    		int c3 = (a3 << 24) | (leaderboard.getTextColour(index + 1) & 0x00FFFFFF);
	    		
	    		if (screen.getDeltaSinceFoundOwnPlayerSearch() >= 20f && !screen.getPlayerToFind().isEmpty()) screen.setPlayerToFind("");
	    		
	    		if (isHovered) {
		    		if (shouldAnimate) RenderUtils.enableScissor(screen, context, cardXOffset - 3, y - 2, cardXOffset + cardWidth + 3, y + cardHeight + 2);
	    			RenderUtils.fill(screen, context, cardXOffset + animationOffset - 3, y - 2, cardXOffset + rankingWidth + animationOffset, y + cardHeight + 2, c1);
		    		RenderUtils.fill(screen, context, cardXOffset + rankingWidth + animationOffset, y - 2, cardXOffset + cardWidth + animationOffset + 3, y + cardHeight + 2, c2);
		    		RenderUtils.renderScaledText(screen, context, leaderboardPositionText, cardXOffset + rankingWidth / 2 - (screen.getTextRenderer().getWidth(leaderboardPositionText) / 2) - 1 + animationOffset, y + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2, 0xFFFFFFFF, 1.1f);
	    		} else {
	    			if (shouldAnimate) RenderUtils.enableScissor(screen, context, cardXOffset, y, cardXOffset + cardWidth, y + cardHeight);
	    			RenderUtils.fill(screen, context, cardXOffset + animationOffset, y, cardXOffset + rankingWidth + animationOffset, y + cardHeight, c1);
		    		RenderUtils.fill(screen, context, cardXOffset + rankingWidth + animationOffset, y, cardXOffset + cardWidth + animationOffset, y + cardHeight, c2);
		    		RenderUtils.renderScaledText(screen, context, leaderboardPositionText, cardXOffset + rankingWidth / 2 - (screen.getTextRenderer().getWidth(leaderboardPositionText) / 2) + animationOffset, y + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2 + 1, c3, 1.0f, false);
	    		}
	    	} else if (isHovered) {
	    		if (shouldAnimate) RenderUtils.enableScissor(screen, context, cardXOffset - 3, y - 2, cardXOffset + cardWidth + 3, y + cardHeight + 2);
	    		RenderUtils.fillGradient(screen, context, cardXOffset + animationOffset - 3, y - 2, cardXOffset + rankingWidth + animationOffset, y + cardHeight + 2, leaderboard.getGradientStart(index + 1), leaderboard.getGradientEnd(index + 1));
	    		RenderUtils.fill(screen, context, cardXOffset + rankingWidth + animationOffset, y - 2, cardXOffset + cardWidth + animationOffset + 3, y + cardHeight + 2, cardHoverColour);
	    		RenderUtils.renderScaledText(screen, context, leaderboardPositionText, cardXOffset + rankingWidth / 2 - (screen.getTextRenderer().getWidth(leaderboardPositionText) / 2) - 1 + animationOffset, y + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2, 0xFFFFFFFF, 1.1f);
	    	} else {
	    		if (shouldAnimate) RenderUtils.enableScissor(screen, context, cardXOffset, y, cardXOffset + cardWidth, y + cardHeight);
	    		RenderUtils.fillGradient(screen, context, cardXOffset + animationOffset, y, cardXOffset + rankingWidth + animationOffset, y + cardHeight, leaderboard.getGradientStart(index + 1), leaderboard.getGradientEnd(index + 1));
	    		RenderUtils.fill(screen, context, cardXOffset + rankingWidth + animationOffset, y, cardXOffset + cardWidth + animationOffset, y + cardHeight, cardColour);
	    		RenderUtils.renderScaledText(screen, context, leaderboardPositionText, cardXOffset + rankingWidth / 2 - (screen.getTextRenderer().getWidth(leaderboardPositionText) / 2) + animationOffset, y + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2 + 1, leaderboard.getTextColour(index + 1), 1.0f, false);
	    	}
	    	
	    	if (isHovered && screen.isMouseDown() && !screen.isDraggingScrollbar() && screen.getDeltaSinceFoundOwnPlayerSearch() > 10) {
    			MinecraftClient.getInstance().setScreen(new ProfileScreen(player.name(), true, gamemode, scrollOffset / (cardHeight + paddingY), screen.getSearchQuery()));
	    	}
	    	
	    	int playerNameStartColour = 0xFFFFFFFF;
	    	int playerNameStopColour = 0xFFFFFFFF;
	    	Text playerNameText = Text.literal(player.name());
	    	
	    	if (gamemode != null) {
	    		Tier tier = player.getTier(gamemode);
	    		
	    		if (tier != Tier.NONE && tier.getIconPath() != null) {
	    			RenderUtils.renderTexture(screen, context, Identifier.of(TierNametags.MODID, player.getTier(gamemode).getIconPath()), cardXOffset + rankingWidth + 7 + animationOffset, y + cardHeight / 2 - 12, 24, 24);
	    		}
	    		
	    		playerNameStartColour = tier.getLightColour();
	    		playerNameStopColour = tier.getDarkColour();
	    	} else {
	    		Tier tier = player.getBestTier();
	    		
	    		if (tier.getIconPath() != null) {
	    			RenderUtils.renderTexture(screen, context, Identifier.of(TierNametags.MODID, tier.getIconPath()), cardXOffset + rankingWidth + animationOffset + 7, y + cardHeight / 2 - 12, 24, 24);
	    		}
	    		
	    		playerNameStartColour = tier.getLightColour();
	    		playerNameStopColour = tier.getDarkColour();
	    		RenderUtils.renderScaledText(screen, context, Text.literal(" • " + player.getLeaderboardPoints() + " pts"), cardXOffset + rankingWidth + animationOffset + 40 + screen.getTextRenderer().getWidth(playerNameText), y + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2, 0xFFFFFFFF, 1.0f);
	    	}
	    	
	    	RenderUtils.renderScaledTextWithGradient(screen, context, playerNameText, cardXOffset + rankingWidth + animationOffset + 40, y + cardHeight / 2 - screen.getTextRenderer().fontHeight / 2, playerNameStartColour, playerNameStopColour, 1.0f);
	    	
	    	SkinTextures skinTextures = SkinCache.getPlayer(player.name());
	    	if (skinTextures != null) RenderUtils.renderTexture(screen, context, skinTextures.texture(), cardXOffset + cardWidth + animationOffset - 27, y + cardHeight / 2 - 12, 24, 24, 24, 24, 192, 192);
	    	else RenderUtils.fill(screen, context, cardXOffset + cardWidth + animationOffset - 27, y + cardHeight / 2 - 12, cardXOffset + cardWidth + animationOffset - 3, y + cardHeight / 2 + 12, 0x0AFFFFFF);
	    	
	    	if (shouldAnimate) context.disableScissor();
	    }

	    if (totalRows > visibleRows) {
	    	double rowHeight = cardHeight + paddingY;
	    	
	        int scrollbarWidth = 6;
	        int scrollbarX = cardXOffset + cardWidth + 8;

	        int scrollbarY = cardInitialY;
	        int scrollbarHeight = (int) (visibleRows * rowHeight);

	        double handleRatio = (double) visibleRows / totalRows;
	        int handleHeight = (int) Math.max(20, scrollbarHeight * handleRatio);

	        double scrollRatio = (maxScroll == 0) ? 0 : screen.getScroll() / maxScroll;
	        int handleY = scrollbarY + (int) ((scrollbarHeight - handleHeight) * scrollRatio);

	        RenderUtils.fill(screen, context, scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 0x80000000);

	        boolean handleHovered = RenderUtils.isMouseHovering(screen, mouseX, mouseY, scrollbarX, handleY, scrollbarX + scrollbarWidth, handleY + handleHeight);
	        int handleColourStart = (handleHovered || screen.isDraggingScrollbar()) ? 0xffffffff : 0xffffffff;
	        int handleColourStop  = (handleHovered || screen.isDraggingScrollbar()) ? 0xffb7a8ff : 0xffffffff;

	        RenderUtils.fillGradient(screen, context, scrollbarX, handleY, scrollbarX + scrollbarWidth, handleY + handleHeight, handleColourStart, handleColourStop);

	        if (handleHovered && screen.isMouseDown() && !screen.isDraggingScrollbar()) {
	            screen.setIsDraggingScrollbar(true);
	            screen.setScrollbarDragOffset((int)(mouseY - handleY));
	            screen.setSearching(false);
	        }

	        if (screen.isDraggingScrollbar() && screen.isMouseDown()) {
	            double mousePos = mouseY - scrollbarY - screen.getScrollbarDragOffset();
	            mousePos = Math.max(0, Math.min(mousePos, scrollbarHeight - handleHeight));

	            double denom = (double)(scrollbarHeight - handleHeight);
	            double newScrollRatio = denom <= 0 ? 0 : mousePos / denom;
	            double newScrollRows = newScrollRatio * maxScroll;

	            screen.setScroll(Math.max(0, Math.min(newScrollRows, maxScroll)));
	        }

	        if (!screen.isMouseDown() && screen.isDraggingScrollbar()) {
	            screen.setIsDraggingScrollbar(false);
	        }
	    }
	}

	private void renderLeaderboardScreen(LeaderboardScreen screen, DrawContext context, int mouseX, int mouseY, float delta) {
		TierNametagsConfig config = instance.getConfig();
		
		RenderUtils.fillGradient(screen, context, 0, 0, RenderUtils.iX, RenderUtils.iY, 0xff9984f9, 0xff050114);
		
		RenderUtils.fill(screen, context, 0, 0, 225, RenderUtils.iY, 0xa0000000);
		RenderUtils.fill(screen, context, 225, 0, 227, RenderUtils.iY, 0x20FFFFFF);
		
		Text title = Text.literal("Leaderboard");
		RenderUtils.renderScaledText(screen, context, title, 225 / 2 - screen.getTextRenderer().getWidth(title), 15, 0xFFFFFFFF, 2.0f);
		
		int buttonColour = 0x35FFFFFF;
		int selectedButtonColour = 0x80FFFFFF;
		int buttonWidth = 200;
		int buttonHeight = 30;
		int buttonXOffset = 225 / 2 - buttonWidth / 2;
		int buttonYOffset = buttonHeight + 58;
		int sidebarGradientStart = 0xff947dff;
		int sidebarGradientEnd = 0xff13083d;
		
		int deltaUntilAnimationComplete = 20;
    	int distanceToMove = buttonWidth + 20;
    	int animationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getSidebarEntriesAnimationDelta(), 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 3) * distanceToMove);
    	int animationOffset = config.reduceMotion ? 0 : -animationDistanceProgress;
		
		boolean hoveringOverallTab = RenderUtils.isMouseHovering(screen, mouseX, mouseY, buttonXOffset + animationOffset, 50, buttonXOffset + buttonWidth + animationOffset, 50 + buttonHeight);
		
		if (hoveringOverallTab || screen.getSelectedTab() == 0) RenderUtils.enableScissor(screen, context, buttonXOffset + animationOffset - 5, 48, buttonXOffset + buttonWidth + animationOffset + 5, 52 + buttonHeight);
		else RenderUtils.enableScissor(screen, context, buttonXOffset + animationOffset, 50, buttonXOffset + buttonWidth + animationOffset, 50 + buttonHeight);
		
		RenderUtils.fillGradient(screen, context, 0, 48, 225, RenderUtils.iY, sidebarGradientStart, sidebarGradientEnd);
		context.disableScissor();
		
		if (hoveringOverallTab || screen.getSelectedTab() == 0) {			
			RenderUtils.fill(screen, context, buttonXOffset + animationOffset - 5, 48, buttonXOffset + buttonWidth + animationOffset + 5, 52 + buttonHeight, screen.getSelectedTab() == 0 ? selectedButtonColour : buttonColour);
			RenderUtils.renderScaledText(screen, context, Text.literal("Overall"), buttonXOffset + animationOffset + 10, buttonYOffset - buttonHeight + 2, 0xFFFFFFFF, 1.5f);
		} else {
			RenderUtils.fill(screen, context, buttonXOffset + animationOffset, 50, buttonXOffset + buttonWidth + animationOffset, 50 + buttonHeight, screen.getSelectedTab() == 0 ? selectedButtonColour : buttonColour);
			RenderUtils.renderScaledText(screen, context, Text.literal("Overall"), buttonXOffset + animationOffset + 10, buttonYOffset - buttonHeight + 2, 0xFFFFFFFF, 1.5f);
		}
		
		if (hoveringOverallTab && screen.isMouseDown()) screen.setSelectedTab(0);
		
		int index = 1;
		for (Gamemode gamemode : Gamemode.values()) {
	    	animationDistanceProgress = distanceToMove - (int) (RenderUtils.easeOut((double) Math.clamp(screen.getSidebarEntriesAnimationDelta() - index, 0, deltaUntilAnimationComplete) / deltaUntilAnimationComplete, 3) * distanceToMove);
	    	animationOffset = config.reduceMotion ? 0 : -animationDistanceProgress;
	    	
			boolean hovering = RenderUtils.isMouseHovering(screen, mouseX, mouseY, buttonXOffset + animationOffset, buttonYOffset, buttonXOffset + buttonWidth + animationOffset, buttonYOffset + buttonHeight);
			
			if (hovering || screen.getSelectedTab() == index) RenderUtils.enableScissor(screen, context, buttonXOffset + animationOffset - 5, buttonYOffset - 2, buttonXOffset + buttonWidth + animationOffset + 5, buttonYOffset + buttonHeight + 2);
			else RenderUtils.enableScissor(screen, context, buttonXOffset + animationOffset, buttonYOffset, buttonXOffset + buttonWidth + animationOffset, buttonYOffset + buttonHeight);
			
			RenderUtils.fillGradient(screen, context, 0, 48, 225, RenderUtils.iY, sidebarGradientStart, sidebarGradientEnd);
			context.disableScissor();
			
			if (hovering || screen.getSelectedTab() == index) {
				RenderUtils.fill(screen, context, buttonXOffset + animationOffset - 5, buttonYOffset - 2, buttonXOffset + buttonWidth + animationOffset + 5, buttonYOffset + buttonHeight + 2, screen.getSelectedTab() == index ? selectedButtonColour : buttonColour);
				RenderUtils.renderScaledText(screen, context, Text.literal(gamemode.getName()), buttonXOffset + animationOffset + 10, buttonYOffset + 10, 0xFFFFFFFF, 1.5f);
				RenderUtils.renderTexture(screen, context, Identifier.of(TierNametags.MODID, gamemode.getIconPath()), buttonXOffset + buttonWidth + animationOffset - 30, buttonYOffset + buttonHeight / 2 - 13, 26, 26);
			} else {				
				RenderUtils.fill(screen, context, buttonXOffset + animationOffset, buttonYOffset, buttonXOffset + buttonWidth + animationOffset, buttonYOffset + buttonHeight, screen.getSelectedTab() == index ? selectedButtonColour : buttonColour);
				RenderUtils.renderScaledText(screen, context, Text.literal(gamemode.getName()), buttonXOffset + animationOffset + 10, buttonYOffset + 10, 0xFFFFFFFF, 1.5f);
				RenderUtils.renderTexture(screen, context, Identifier.of(TierNametags.MODID, gamemode.getIconPath()), buttonXOffset + buttonWidth + animationOffset - 30, buttonYOffset + buttonHeight / 2 - 12, 24, 24);
			}
			
			if (hovering && screen.isMouseDown()) screen.setSelectedTab(index);
			
			buttonYOffset += buttonHeight + 8;
			index++;
		}
		
		if (screen.getSelectedTab() == 0) this.renderLeaderboard(screen, context, mouseX, mouseY, delta, null, instance.getLeaderboard().getPlayers());
		else {
			Gamemode gamemode = Gamemode.values()[screen.getSelectedTab() - 1];
			this.renderLeaderboard(screen, context, mouseX, mouseY, delta, gamemode, instance.getLeaderboard().getPlayers(gamemode));
		}
	}
	
	public void renderSettingsScreen(SettingsScreen screen, DrawContext context, int mouseX, int mouseY, float delta) {
		RenderUtils.fillGradient(screen, context, 0, 0, RenderUtils.iX, RenderUtils.iY, 0xff9984f9, 0xff050114);
		
		Text settingsText = Text.literal("Settings");
		RenderUtils.renderScaledText(screen, context, settingsText, RenderUtils.iX / 2 - screen.getTextRenderer().getWidth(settingsText) / 2 * 3, 25, 0xFFFFFFFF, 3f);
		
		int panelWidth = 325;
		int panelHeight = 228;
		int panelX = RenderUtils.iX / 2 - panelWidth / 2;
		int panelY = 70;
		
		RenderUtils.fill(screen, context, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x80000000);
		
		RenderUtils.renderScaledText(screen, context, Text.literal("Tier Display"), panelX + 10, panelY + 10, 0xFFFFFFFF, 1.3f);
		RenderUtils.fill(screen, context, panelX + 5, panelY + 45, panelX + panelWidth - 10, panelY + 138, 0x20000000);
		
		int accessibilityPanelY = panelY + 138;
		
		RenderUtils.renderScaledText(screen, context, Text.literal("Accessibility"), panelX + 10, accessibilityPanelY + 10, 0xFFFFFFFF, 1.3f);
		RenderUtils.fill(screen, context, panelX + 5, accessibilityPanelY + 25, panelX + panelWidth - 10, accessibilityPanelY + 45, 0x20000000);
		
		screen.renderSettings(context, mouseX, mouseY, delta);
	}
	
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V")
	public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
		Screen screen = (Screen) ((Object) this);
		if (screen instanceof ProfileScreen) this.renderProfileScreen((ProfileScreen) screen, context, mouseX, mouseY, delta);
		else if (screen instanceof LeaderboardScreen) this.renderLeaderboardScreen((LeaderboardScreen) screen, context, mouseX, mouseY, delta);
		else if (screen instanceof SettingsScreen) this.renderSettingsScreen(((SettingsScreen) screen), context, mouseX, mouseY, delta);
	}
}
