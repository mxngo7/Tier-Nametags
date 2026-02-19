package me.mxngo.ui.screens;

import me.mxngo.TierNametags;
import me.mxngo.config.Tierlist;
import me.mxngo.tiers.TierTest;
import me.mxngo.tiers.TieredPlayer;
import me.mxngo.tiers.wrappers.MCTiersAPIWrapper;
import me.mxngo.ui.ITierNametagsScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TierTestHistoryScreen extends Screen implements ITierNametagsScreen {
	
	public MinecraftClient mc = MinecraftClient.getInstance();
	private TierNametags instance = TierNametags.getInstance();
	
	public TieredPlayer player;
	private TierTest[] tests;
	private boolean loading = true;
	
	public double scrollOffset = 0;
	public double targetScrollOffset = 0;
	public double maxScroll = 0;
	
	private float animationDelta = 0.0f;
	
	public TierTestHistoryScreen(TieredPlayer player) {
		super(Text.literal("Tier Test History"));		
		this.player = player;
		this.fetchTierTests();
	}
	
	private void fetchTierTests() {
		if (this.player == null) {
			mc.execute(() -> {
				mc.setScreen(null);
			});
			return;
		}
		
		((MCTiersAPIWrapper) instance.tierlistManager.getAPIWrapper(Tierlist.MCTIERS)).getTierTests(this.player)
			.thenAccept(tests -> {
				this.tests = tests;
				this.loading = false;
			}).exceptionally(exception -> {
				exception.printStackTrace();
				return null;
			});
	}
	
	public TierTest[] getTierTests() {
		if (loading) return new TierTest[] {};
		return this.tests;
	}
	
	public boolean isLoading() {
		return this.loading;
	}
	
	public float getAnimationDelta() {
		return this.animationDelta;
	}
	
	@Override
    public boolean shouldPause() {
        return false;
    }
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
	    this.targetScrollOffset -= verticalAmount * 30;
	    return true;
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		if (!this.isLoading()) this.animationDelta += delta;
	}

	@Override
	public TextRenderer getTextRenderer() {
		return this.textRenderer;
	}
	
}
