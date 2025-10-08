package me.mxngo.ui.screens;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import me.mxngo.TierNametags;
import me.mxngo.config.ConfigManager;
import me.mxngo.config.DisplayType;
import me.mxngo.config.TierNametagsConfig;
import me.mxngo.config.TierPosition;
import me.mxngo.ocetiers.Gamemode;
import me.mxngo.ui.components.ModeComponent;
import me.mxngo.ui.components.SwitchComponent;
import me.mxngo.ui.components.TabContentComponent;
import me.mxngo.ui.util.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen {
	private static int selectedTab = 0;
	
	private TierNametagsConfig updatedConfig = TierNametags.getInstance().getConfig().copy();
	private boolean mouseDown = false;
	
	public DisplayModeTabComponent nametagsTabContent, playerListTabContent, chatTabContent;
	public SwitchComponent reducedMotion;
	public ModeComponent<Gamemode> gamemodeSelector;
	
	public SettingsScreen() {
		super(Text.literal("Settings"));
	}
	
	public TierNametagsConfig getUpdatedConfig() {
		return updatedConfig;
	}
	
	public boolean isMouseDown() {
		return mouseDown;
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
		
		int yOffset = 120;
		
		SettingsScreen screen = this;
		
		nametagsTabContent = new DisplayModeTabComponent() {
			{				
				enabled = new SwitchComponent(screen, Text.literal("Enabled"), RenderUtils.iX / 2 - 152, yOffset, a, b, c, d, e, f, g, h, updatedConfig.nametags.enabled());
				displayTierText = new SwitchComponent(screen, Text.literal("Tier Text"), RenderUtils.iX / 2 - 152, yOffset + 14, a, b, c, d, e, f, g, h, updatedConfig.nametags.tierText());
				displayTierIcon = new SwitchComponent(screen, Text.literal("Tier Icon"), RenderUtils.iX / 2 - 152, yOffset + 28, a, b, c, d, e, f, g, h, updatedConfig.nametags.tierIcon());
				displayGamemodeIcon = new SwitchComponent(screen, Text.literal("Gamemode Icon"), RenderUtils.iX / 2 - 152, yOffset + 42, a, b, c, d, e, f, g, h, updatedConfig.nametags.gamemodeIcon());
				tierPosition = new SwitchComponent(screen, Text.literal("Tier Position: " + updatedConfig.nametags.position().toString().toLowerCase()), RenderUtils.iX / 2 - 152, yOffset + 56, a, b, c, d, e, f, g, h, updatedConfig.nametags.position() == TierPosition.RIGHT);
			}
			
			@Override
			public void tick() {
				if (tierPosition.isToggled()) tierPosition.setLabel("Tier Position: right");
				else tierPosition.setLabel("Tier Position: left");
			}
			
			@Override
			public void render(DrawContext context, int mouseX, int mouseY, float delta) {
				enabled.render(context, mouseX, mouseY, delta);
				displayTierText.render(context, mouseX, mouseY, delta);
				displayTierIcon.render(context, mouseX, mouseY, delta);
				displayGamemodeIcon.render(context, mouseX, mouseY, delta);
				tierPosition.render(context, mouseX, mouseY, delta);
			}

			@Override
			public void mouseClicked(double mouseX, double mouseY, int button) {
				enabled.mouseClicked(mouseX, mouseY, button);
				displayTierText.mouseClicked(mouseX, mouseY, button);
				displayTierIcon.mouseClicked(mouseX, mouseY, button);
				displayGamemodeIcon.mouseClicked(mouseX, mouseY, button);
				tierPosition.mouseClicked(mouseX, mouseY, button);
			}
		};
		
		playerListTabContent = new DisplayModeTabComponent() {
			{				
				enabled = new SwitchComponent(screen, Text.literal("Enabled"), RenderUtils.iX / 2 - 152, yOffset, a, b, c, d, e, f, g, h, updatedConfig.playerList.enabled());
				displayTierText = new SwitchComponent(screen, Text.literal("Tier Text"), RenderUtils.iX / 2 - 152, yOffset + 14, a, b, c, d, e, f, g, h, updatedConfig.playerList.tierText());
				displayTierIcon = new SwitchComponent(screen, Text.literal("Tier Icon"), RenderUtils.iX / 2 - 152, yOffset + 28, a, b, c, d, e, f, g, h, updatedConfig.playerList.tierIcon());
				displayGamemodeIcon = new SwitchComponent(screen, Text.literal("Gamemode Icon"), RenderUtils.iX / 2 - 152, yOffset + 42, a, b, c, d, e, f, g, h, updatedConfig.playerList.gamemodeIcon());
				tierPosition = new SwitchComponent(screen, Text.literal("Tier Position: " + updatedConfig.playerList.position().toString().toLowerCase()), RenderUtils.iX / 2 - 152, yOffset + 56, a, b, c, d, e, f, g, h, updatedConfig.playerList.position() == TierPosition.RIGHT);
			}
			
			@Override
			public void tick() {
				if (tierPosition.isToggled()) tierPosition.setLabel("Tier Position: right");
				else tierPosition.setLabel("Tier Position: left");
			}
			
			@Override
			public void render(DrawContext context, int mouseX, int mouseY, float delta) {
				enabled.render(context, mouseX, mouseY, delta);
				displayTierText.render(context, mouseX, mouseY, delta);
				displayTierIcon.render(context, mouseX, mouseY, delta);
				displayGamemodeIcon.render(context, mouseX, mouseY, delta);
				tierPosition.render(context, mouseX, mouseY, delta);
			}
			
			@Override
			public void mouseClicked(double mouseX, double mouseY, int button) {
				enabled.mouseClicked(mouseX, mouseY, button);
				displayTierText.mouseClicked(mouseX, mouseY, button);
				displayTierIcon.mouseClicked(mouseX, mouseY, button);
				displayGamemodeIcon.mouseClicked(mouseX, mouseY, button);
				tierPosition.mouseClicked(mouseX, mouseY, button);
			}
		};
		
		chatTabContent = new DisplayModeTabComponent() {
			{				
				enabled = new SwitchComponent(screen, Text.literal("Enabled"), RenderUtils.iX / 2 - 152, yOffset, a, b, c, d, e, f, g, h, updatedConfig.chat.enabled());
				displayTierText = new SwitchComponent(screen, Text.literal("Tier Text"), RenderUtils.iX / 2 - 152, yOffset + 14, a, b, c, d, e, f, g, h, updatedConfig.chat.tierText());
				displayTierIcon = new SwitchComponent(screen, Text.literal("Tier Icon"), RenderUtils.iX / 2 - 152, yOffset + 28, a, b, c, d, e, f, g, h, updatedConfig.chat.tierIcon());
				displayGamemodeIcon = new SwitchComponent(screen, Text.literal("Gamemode Icon"), RenderUtils.iX / 2 - 152, yOffset + 42, a, b, c, d, e, f, g, h, updatedConfig.chat.gamemodeIcon());
				tierPosition = new SwitchComponent(screen, Text.literal("Tier Position: " + updatedConfig.chat.position().toString().toLowerCase()), RenderUtils.iX / 2 - 152, yOffset + 56, a, b, c, d, e, f, g, h, updatedConfig.chat.position() == TierPosition.RIGHT);
			}
			
			@Override
			public void tick() {
				if (tierPosition.isToggled()) tierPosition.setLabel("Tier Position: right");
				else tierPosition.setLabel("Tier Position: left");
			}
			
			@Override
			public void render(DrawContext context, int mouseX, int mouseY, float delta) {
				enabled.render(context, mouseX, mouseY, delta);
				displayTierText.render(context, mouseX, mouseY, delta);
				displayTierIcon.render(context, mouseX, mouseY, delta);
				displayGamemodeIcon.render(context, mouseX, mouseY, delta);
				tierPosition.render(context, mouseX, mouseY, delta);
			}
			
			@Override
			public void mouseClicked(double mouseX, double mouseY, int button) {
				enabled.mouseClicked(mouseX, mouseY, button);
				displayTierText.mouseClicked(mouseX, mouseY, button);
				displayTierIcon.mouseClicked(mouseX, mouseY, button);
				displayGamemodeIcon.mouseClicked(mouseX, mouseY, button);
				tierPosition.mouseClicked(mouseX, mouseY, button);
			}
		};
		
		gamemodeSelector = new ModeComponent<>(this, List.of(Gamemode.values()), updatedConfig.gamemode, Gamemode::getName, Gamemode::getStyledIcon, RenderUtils.iX / 2 - 152, yOffset + 70);
		reducedMotion = new SwitchComponent(this, Text.literal("Reduced Motion"), RenderUtils.iX / 2 - 152, yOffset + 118, a, b, c, d, e, f, g, h, updatedConfig.reduceMotion);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		nametagsTabContent.tick();
		playerListTabContent.tick();
		chatTabContent.tick();
	}
	
	public void renderSettings(DrawContext context, int mouseX, int mouseY, float delta) {
		if (selectedTab == 0) nametagsTabContent.render(context, mouseX, mouseY, delta);
		else if (selectedTab == 1) playerListTabContent.render(context, mouseX, mouseY, delta);
		else if (selectedTab == 2) chatTabContent.render(context, mouseX, mouseY, delta);
		
		reducedMotion.render(context, mouseX, mouseY, delta);
		gamemodeSelector.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		int panelWidth = 325;
		int panelX = RenderUtils.iX / 2 - panelWidth / 2;
		int panelY = 70;
		int buttonWidth = (panelWidth - 15) / 3;
		int panelHeight = 228;
		int buttonColour = 0x40000000;
		int buttonHoverColour = 0x30000000;
		int buttonSelectedColour = 0x20000000;
		int buttonSelectedHoverColour = 0x15000000;
		
		boolean hoveringNametagTab = RenderUtils.isMouseHovering(this, mouseX, mouseY, panelX + 5, panelY + 25, panelX + buttonWidth, panelY + 45);
		boolean hoveringTablistTab = RenderUtils.isMouseHovering(this, mouseX, mouseY, panelX + 5 + buttonWidth - 1, panelY + 25, panelX + 5 + buttonWidth * 2, panelY + 45);
		boolean hoveringChatTab = RenderUtils.isMouseHovering(this, mouseX, mouseY, panelX + 10 + buttonWidth * 2, panelY + 25, panelX + panelWidth - 10, panelY + 45);
		
		RenderUtils.fill(this, context, panelX + 5, panelY + 25, panelX + buttonWidth, panelY + 45, (selectedTab == 0 ? (hoveringNametagTab ? buttonSelectedHoverColour : buttonSelectedColour) : (hoveringNametagTab ? buttonHoverColour : buttonColour)));
		
		Text nametagsText = Text.literal("Nametags");
		RenderUtils.renderScaledText(this, context, nametagsText, panelX + 5 + buttonWidth / 2 - textRenderer.getWidth(nametagsText) / 2, panelY + 35 - textRenderer.fontHeight / 2, 0xFFFFFFFF, 1f);
		
		RenderUtils.fill(this, context, panelX + 4 + buttonWidth, panelY + 25, panelX + 5 + buttonWidth * 2, panelY + 45, (selectedTab == 1 ? (hoveringTablistTab ? buttonSelectedHoverColour : buttonSelectedColour) : (hoveringTablistTab ? buttonHoverColour : buttonColour)));
		
		Text tablistText = Text.literal("Player List");
		RenderUtils.renderScaledText(this, context, tablistText, panelX + 5 + buttonWidth * 3 / 2 - textRenderer.getWidth(tablistText) / 2, panelY + 35 - textRenderer.fontHeight / 2, 0xFFFFFFFF, 1f);
		
		RenderUtils.fill(this, context, panelX + 10 + buttonWidth * 2, panelY + 25, panelX + panelWidth - 10, panelY + 45, (selectedTab == 2 ? (hoveringChatTab ? buttonSelectedHoverColour : buttonSelectedColour) : (hoveringChatTab ? buttonHoverColour : buttonColour)));
		
		Text chatText = Text.literal("Chat");
		RenderUtils.renderScaledText(this, context, chatText, panelX + buttonWidth + panelWidth / 2 - textRenderer.getWidth(chatText) / 2, panelY + 35 - textRenderer.fontHeight / 2, 0xFFFFFFFF, 1f);
	
		int saveButtonWidth = 70;
		int saveButtonHeight = 20;
		
		boolean hoveringSaveButton = RenderUtils.isMouseHovering(this, mouseX, mouseY, panelX + panelWidth - saveButtonWidth - 5, panelY + panelHeight - saveButtonHeight - 5, panelX + panelWidth - 5, panelY + panelHeight - 5);
		
		RenderUtils.fill(this, context, panelX + panelWidth - saveButtonWidth - 5, panelY + panelHeight - saveButtonHeight - 5, panelX + panelWidth - 5, panelY + panelHeight - 5, hoveringSaveButton ? 0x20000000 : 0x40000000);
		
		Text saveText = Text.literal("Save & Exit");
		RenderUtils.renderScaledText(this, context, saveText, panelX + panelWidth - saveButtonWidth / 2 - 5 - (int) (textRenderer.getWidth(saveText) * 0.8 / 2), panelY + panelHeight - saveButtonHeight / 2 - 5 - (int) (textRenderer.fontHeight * 0.8 / 2), 0xFFFFFFFF, 0.8f);
	}
	
	private void save() {
		updatedConfig.nametags = new DisplayType(
			nametagsTabContent.enabled.isToggled(),
			nametagsTabContent.displayTierIcon.isToggled(),
			nametagsTabContent.displayGamemodeIcon.isToggled(),
			nametagsTabContent.displayTierText.isToggled(),
			nametagsTabContent.tierPosition.isToggled() ? TierPosition.RIGHT : TierPosition.LEFT
		);
		
		updatedConfig.playerList = new DisplayType(
			playerListTabContent.enabled.isToggled(),
			playerListTabContent.displayTierIcon.isToggled(),
			playerListTabContent.displayGamemodeIcon.isToggled(),
			playerListTabContent.displayTierText.isToggled(),
			playerListTabContent.tierPosition.isToggled() ? TierPosition.RIGHT : TierPosition.LEFT
		);
		
		updatedConfig.chat = new DisplayType(
			chatTabContent.enabled.isToggled(),
			chatTabContent.displayTierIcon.isToggled(),
			chatTabContent.displayGamemodeIcon.isToggled(),
			chatTabContent.displayTierText.isToggled(),
			chatTabContent.tierPosition.isToggled() ? TierPosition.RIGHT : TierPosition.LEFT
		);
		
		updatedConfig.gamemode = gamemodeSelector.getMode();
		updatedConfig.reduceMotion = reducedMotion.isToggled();
				
		ConfigManager.getInstance().saveConfig(updatedConfig);
		this.close();
	}
	
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		
		if (selectedTab == 0) nametagsTabContent.mouseClicked(mouseX, mouseY, button);
		else if (selectedTab == 1) playerListTabContent.mouseClicked(mouseX, mouseY, button);
		else if (selectedTab == 2) chatTabContent.mouseClicked(mouseX, mouseY, button);
		
		reducedMotion.mouseClicked(mouseX, mouseY, button);
		gamemodeSelector.mouseClicked(mouseX, mouseY, button);
		
		int panelWidth = 325;
		int panelX = RenderUtils.iX / 2 - panelWidth / 2;
		int panelY = 70;
		int buttonWidth = (panelWidth - 15) / 3;
		
		boolean hoveringNametagTab = RenderUtils.isMouseHovering(this, mouseX, mouseY, panelX + 5, panelY + 25, panelX + buttonWidth, panelY + 45);
		boolean hoveringTablistTab = RenderUtils.isMouseHovering(this, mouseX, mouseY, panelX + 5 + buttonWidth - 1, panelY + 25, panelX + 5 + buttonWidth * 2, panelY + 45);
		boolean hoveringChatTab = RenderUtils.isMouseHovering(this, mouseX, mouseY, panelX + 10 + buttonWidth * 2, panelY + 25, panelX + panelWidth - 10, panelY + 45);
		
		if (hoveringNametagTab) selectedTab = 0;
		else if (hoveringTablistTab) selectedTab = 1;
		else if (hoveringChatTab) selectedTab = 2;
		
		int saveButtonWidth = 70;
		int saveButtonHeight = 20;
		int panelHeight = 228;
		
		boolean hoveringSaveButton = RenderUtils.isMouseHovering(this, mouseX, mouseY, panelX + panelWidth - saveButtonWidth - 5, panelY + panelHeight - saveButtonHeight - 5, panelX + panelWidth - 5, panelY + panelHeight - 5);
		
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			mouseDown = true;
			
			if (hoveringSaveButton) this.save();
		}
		
		return super.mouseClicked(click, doubled);
	}
	
	@Override
	public boolean mouseReleased(Click click) {
		int button = click.button();
		
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) mouseDown = false;
		
		return super.mouseReleased(click);
	}
	
	private abstract class DisplayModeTabComponent extends TabContentComponent {
		public SwitchComponent enabled, displayTierText, displayTierIcon, displayGamemodeIcon, tierPosition;
	}
}