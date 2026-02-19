package me.mxngo.ui.screens;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import me.mxngo.TierNametags;
import me.mxngo.tiers.Gamemode;
import me.mxngo.tiers.TieredPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class LeaderboardScreen extends Screen implements ITierNametagsScreen {
	private TierNametags instance = TierNametags.getInstance();
	public MinecraftClient mc = MinecraftClient.getInstance();
	
	private boolean mouseDown = false;
	private int selectedTab = 0;
	
	private double scroll = 0;
	private boolean draggingScrollbar = false;
	private int scrollbarDragOffset = 0;
	
	private boolean searching = false;
	private float deltaSinceToggledSearching = 0;
	
	private String searchQuery = "";
	private float deltaSinceFoundOwnPlayerSearch = 20;
	
	private float leaderboardEntriesAnimationDelta = 0.0f;
	private float sidebarEntriesAnimationDelta = 0.0f;
	
	private String playerToFind = "";
	
	public LeaderboardScreen() {
		super(Text.literal("Leaderboard"));
	}
	
	public boolean isMouseDown() {
		return this.mouseDown;
	}
	
	public int getSelectedTab() {
		return this.selectedTab;
	}
	
	public double getScroll() {
		return this.scroll;
	}
	
	public boolean isDraggingScrollbar() {
		return this.draggingScrollbar;
	}
	
	public int getScrollbarDragOffset() {
		return this.scrollbarDragOffset;
	}
	
	public boolean isSearching() {
		return this.searching;
	}
	
	public float getDeltaSinceToggledSearching() {
		return this.deltaSinceToggledSearching;
	}
	
	public String getSearchQuery() {
		return this.searchQuery;
	}
	
	public float getDeltaSinceFoundOwnPlayerSearch() {
		return this.deltaSinceFoundOwnPlayerSearch;
	}
	
	public float getLeaderboardEntriesAnimationDelta() {
		return this.leaderboardEntriesAnimationDelta;
	}
	
	public float getSidebarEntriesAnimationDelta() {
		return this.sidebarEntriesAnimationDelta;
	}
	
	public String getPlayerToFind() {
		return this.playerToFind;
	}
	
	public TextRenderer getTextRenderer() {
		return this.textRenderer;
	}
	
	public void setSelectedTab(int tab) {
		this.scroll = 0;
		
//		if (this.leaderboardEntriesAnimationDelta > 40.0f)
//			this.leaderboardEntriesAnimationDelta = 0.0f;
		
		this.selectedTab = tab;
	}
	
	public void setScroll(double scroll) {
		this.scroll = scroll;
	}
	
	public void setIsDraggingScrollbar(boolean dragging) {
		this.draggingScrollbar = dragging;
	}
	
	public void setScrollbarDragOffset(int offset) {
		this.scrollbarDragOffset = offset;
	}
	
	public void setSearching(boolean searching) {
		this.searching = searching;
	}
	
	public void setDeltaSinceToggledSearching(float delta) {
		this.deltaSinceToggledSearching = delta;
	}
	
	public void setSearchQuery(String query) {
		if (!this.searchQuery.isEmpty() && query.isEmpty()) this.leaderboardEntriesAnimationDelta = 0.0f;
		this.searchQuery = query;
	}
	
	public void setDeltaSinceFoundOwnPlayerSearch(float delta) {
		this.deltaSinceFoundOwnPlayerSearch = delta;
	}
	
	public void setPlayerToFind(String name) {
		this.playerToFind = name;
	}
	
	public void toggleSearching() {
		if (this.deltaSinceToggledSearching > 0.25) {
			this.searching = !this.searching;
			this.deltaSinceToggledSearching = 0.0f;
		}
	}
	
	private void backspaceSearchInput() {
		if (!this.searchQuery.isEmpty()) this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
	}
	
	public String getAutocompletion(String query, List<TieredPlayer> players) {
		for (TieredPlayer player : players) {
			if (player.name().toLowerCase().startsWith(query.toLowerCase())) return player.name().substring(query.length());
		}
		
		return "";
	}
	
	public String getPlayerAutocompletion(String query, List<TieredPlayer> players) {
		for (TieredPlayer player : players) {
			if (player.name().toLowerCase().startsWith(query.toLowerCase())) return player.name();
		}
		
		return "";
	}
	
	private List<TieredPlayer> getPlayers() {
		if (this.selectedTab == 0) return instance.tierlistManager.getActiveLeaderboard().getPlayers();
		else return instance.tierlistManager.getActiveLeaderboard().getPlayers(Gamemode.values()[this.selectedTab - 1]);
	}
	
	@Override
    protected void init() {
    	super.init();
    }
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		leaderboardEntriesAnimationDelta += delta;
		sidebarEntriesAnimationDelta += delta;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) this.mouseDown = true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) this.mouseDown = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		this.searching = false;
		this.scroll -= verticalAmount;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	public void onKey(long window, int key, int scancode, int action, int modifiers) {
		if (action == GLFW.GLFW_REPEAT) {
			if (this.searching && key == GLFW.GLFW_KEY_BACKSPACE) this.backspaceSearchInput();
		} else if (!(action == GLFW.GLFW_PRESS)) return;
		
		String validCharacters = "qwertyuiopasdfghjklzxcvbnm_1234567890";
		
		if (this.searching) {
			if (key == GLFW.GLFW_KEY_TAB) {
				String autocompletion = this.getPlayerAutocompletion(this.searchQuery, this.getPlayers());
				if (!autocompletion.isEmpty()) this.searchQuery = autocompletion;
			} else {				
				if (key == GLFW.GLFW_KEY_BACKSPACE) this.backspaceSearchInput();
				char value = (char) key;
				if ((modifiers & GLFW.GLFW_MOD_SHIFT) == 0) value = String.valueOf(value).toLowerCase().toCharArray()[0];
				else if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 && String.valueOf(value).equals("-")) value = '_';
				if (!(validCharacters.contains(String.valueOf(value).toLowerCase()))) return;
				this.searchQuery += value;
			}
		}
	}
}
