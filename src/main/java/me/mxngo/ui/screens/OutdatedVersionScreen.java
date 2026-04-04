package me.mxngo.ui.screens;

import org.lwjgl.glfw.GLFW;

import me.mxngo.ui.ITierNametagsScreen;
import me.mxngo.ui.util.RenderUtils;
import me.mxngo.update.VersionChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class OutdatedVersionScreen extends Screen implements ITierNametagsScreen {
	private boolean hoveringDownloadButton = false;
	private boolean hoveringIgnoreButton = false;
	
	public OutdatedVersionScreen() {
		super(Text.literal("Version Outdated"));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		String latestVersion = VersionChecker.getLatestVersion().get().split("\\+")[0];
		
		Text outdatedText = Text.literal("Tier Nametags is Outdated!");
		int headerX = RenderUtils.iX / 2 - this.textRenderer.getWidth(outdatedText);
		int headerY = RenderUtils.iY / 4;
		RenderUtils.renderScaledText(this, context, outdatedText, headerX, headerY, 0xFFFFFFFF, 2.0f);
		
		Text downloadButtonText = Text.literal("Download v" + latestVersion);
		Text ignoreButtonText = Text.literal("Ignore");
		
		int textHeight = this.textRenderer.fontHeight;
		
		int buttonColour = 0x20ffffff;
		int buttonHoverColour = 0x40ffffff;
		
		int buttonWidth = this.textRenderer.getWidth(downloadButtonText) + 20;
		int buttonHeight = 20;
		int buttonY = headerY + 50;
		
		int downloadButtonX = RenderUtils.iX / 2 - buttonWidth - 5;
		int ignoreButtonX = RenderUtils.iX / 2 + 5;
		
		hoveringDownloadButton = RenderUtils.isMouseHovering(this, mouseX, mouseY, downloadButtonX, buttonY, downloadButtonX + buttonWidth, buttonY + buttonHeight);
		hoveringIgnoreButton = RenderUtils.isMouseHovering(this, mouseX, mouseY, ignoreButtonX, buttonY, ignoreButtonX + buttonWidth, buttonY + buttonHeight);
		
		RenderUtils.fill(this, context, downloadButtonX, buttonY, downloadButtonX + buttonWidth, buttonY + buttonHeight, hoveringDownloadButton ? buttonHoverColour : buttonColour);
		RenderUtils.renderScaledText(this, context, downloadButtonText, downloadButtonX + 10, buttonY + textHeight / 2 + 2, 0xFFFFFFFF, 1.0f);
		
		RenderUtils.fill(this, context, ignoreButtonX, buttonY, ignoreButtonX + buttonWidth, buttonY + buttonHeight, hoveringIgnoreButton ? buttonHoverColour : buttonColour);
		RenderUtils.renderScaledText(this, context, ignoreButtonText, ignoreButtonX + (buttonWidth - 10) / 2 - this.textRenderer.getWidth(ignoreButtonText) / 2 + 5, buttonY + textHeight / 2 + 2, 0xFFFFFFFF, 1.0f);
	}
	
	@Override
	public TextRenderer getTextRenderer() {
		return this.textRenderer;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			if (hoveringDownloadButton) {
				String url = VersionChecker.getLatestVersionDownloadUrl();
				
				MinecraftClient.getInstance().setScreen(
				    new ConfirmLinkScreen(
				        open -> {
				            if (open) Util.getOperatingSystem().open(url);
				            MinecraftClient.getInstance().setScreen(null);
				        },
				        url,
				        true
				    )
				);
			} else if (hoveringIgnoreButton) {
				this.close();
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
}
