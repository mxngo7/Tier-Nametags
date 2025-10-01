package me.mxngo.ui.screens;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import me.mxngo.TierNametags;
import me.mxngo.config.TierNametagsConfig;
import me.mxngo.config.TierPosition;
import me.mxngo.ocetiers.Gamemode;
import me.mxngo.ui.components.ModeComponent;
import me.mxngo.ui.components.SwitchComponent;
import me.mxngo.ui.util.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen {
	private TierNametagsConfig updatedConfig = TierNametags.getInstance().getConfig().copy();
	private boolean mouseDown = false;
	
	public SwitchComponent showOnNametags, displayTierInTablist, displayTierInChat, displayTierText, tierPosition, reducedMotion;
	public ModeComponent<Gamemode> tierGamemode;
	
	public SettingsScreen() {
		super(Text.literal("Settings"));
	}
	
	public TierNametagsConfig getUpdatedConfig() {
		return updatedConfig;
	}
	
	public boolean isMouseDown() {
		return mouseDown;
	}
	
	public void renderSettings(DrawContext context, int mouseX, int mouseY, float delta) {
		showOnNametags.render(context, mouseX, mouseY, delta);
		displayTierInTablist.render(context, mouseX, mouseY, delta);
		displayTierText.render(context, mouseX, mouseY, delta);
		displayTierInChat.render(context, mouseX, mouseY, delta);
		tierPosition.render(context, mouseX, mouseY, delta);
		reducedMotion.render(context, mouseX, mouseY, delta);
		tierGamemode.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	protected void init() {
		super.init();
		
		int a = 0xdc9b42f5; // Switch On
		int b = 0xff000000; // Switch Off
		int c = 0xdcffffff; // Handle On
		int d = 0xdcffffff; // Handle Off
		
		int e = 0xdca652fa; // Switch On (Hover)
		int f = 0xff1c1c1c; // Switch Off (Hover)
		int g = 0xdcffffff; // Handle On (Hover)
		int h = 0xdcffffff; // Handle Off (Hover)
		
		showOnNametags = new SwitchComponent(this, Text.literal("Display On Nametags"), RenderUtils.iX / 2 - 152, 100, 0, a, b, c, d, e, f, g, h, updatedConfig.showOnNametags);
		displayTierInTablist = new SwitchComponent(this, Text.literal("Display In Tablist"), RenderUtils.iX / 2 - 152, 114, 1, a, b, c, d, e, f, g, h, updatedConfig.showOnTablist);
		displayTierInChat = new SwitchComponent(this, Text.literal("Display In Chat"), RenderUtils.iX / 2 - 152, 128, 0, a, b, c, d, e, f, g, h, updatedConfig.showInChat);
		displayTierText = new SwitchComponent(this, Text.literal("Display Tier Text"), RenderUtils.iX / 2 - 152, 142, 0, a, b, c, d, e, f, g, h, updatedConfig.showTierText);
		tierPosition = new SwitchComponent(this, Text.literal("Tier Position: " + updatedConfig.tierPosition.toString().toLowerCase()), RenderUtils.iX / 2 - 152, 156, 0, a, b, c, d, e, f, g, h, updatedConfig.tierPosition == TierPosition.RIGHT);
		reducedMotion = new SwitchComponent(this, Text.literal("Reduced Motion"), RenderUtils.iX / 2 - 152, 220, 0, a, b, c, d, e, f, g, h, updatedConfig.reduceMotion);
		tierGamemode = new ModeComponent<Gamemode>(this, List.of(Gamemode.values()), updatedConfig.tierGamemode, Gamemode::getName, Gamemode::getStyledIcon, RenderUtils.iX / 2 - 152, 170);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		updatedConfig.showOnNametags = showOnNametags.isToggled();
		updatedConfig.showOnTablist = displayTierInTablist.isToggled();
		updatedConfig.showInChat = displayTierInChat.isToggled();
		updatedConfig.showTierText = displayTierText.isToggled();
		updatedConfig.reduceMotion = reducedMotion.isToggled();
		
		if (tierPosition.isToggled()) {
			tierPosition.setLabel("Tier Position: right");
			updatedConfig.tierPosition = TierPosition.RIGHT;
		} else {
			tierPosition.setLabel("Tier Position: left");
			updatedConfig.tierPosition = TierPosition.LEFT;
		}
		
		updatedConfig.tierGamemode = tierGamemode.getMode();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		showOnNametags.mouseClicked(mouseX, mouseY, button);
		displayTierInTablist.mouseClicked(mouseX, mouseY, button);
		displayTierText.mouseClicked(mouseX, mouseY, button);
		displayTierInChat.mouseClicked(mouseX, mouseY, button);
		tierPosition.mouseClicked(mouseX, mouseY, button);
		reducedMotion.mouseClicked(mouseX, mouseY, button);
		tierGamemode.mouseClicked(mouseX, mouseY, button);
		
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) mouseDown = true;
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) mouseDown = false;
		
		return super.mouseReleased(mouseX, mouseY, button);
	}
}