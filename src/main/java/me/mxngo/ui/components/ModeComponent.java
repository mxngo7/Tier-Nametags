package me.mxngo.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import me.mxngo.ui.IComponent;
import me.mxngo.ui.screens.ITierNametagsScreen;
import me.mxngo.ui.util.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ModeComponent<T, S extends Screen & ITierNametagsScreen> implements IComponent {
	private S parent;
	
	private List<T> modes = new ArrayList<>();
	private T mode;
	private Function<T, String> modeNameSupplier;
	private Function<T, Text> iconSupplier;
	private int longestModeName;
	
	public int x, y;
	
	public ModeComponent(S parent, List<T> modes, T defaultMode, Function<T, String> modeNameSupplier, Function<T, Text> iconSupplier, int x, int y) {
		this.parent = parent;
		this.modes = modes;
		this.mode = defaultMode;
		this.modeNameSupplier = modeNameSupplier;
		this.iconSupplier = iconSupplier;
		this.x = x;
		this.y = y;
		this.longestModeName = modes.stream().mapToInt(mode -> parent.getTextRenderer().getWidth(Text.empty().append(iconSupplier.apply(mode)).append(" ").append(modeNameSupplier.apply(mode)))).sorted().max().orElseThrow();
	}
	
	public T getMode() {
		return mode;
	}

	@Override
	public void tick() {}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		String modeName = this.modeNameSupplier.apply(mode);
		Text iconText = this.iconSupplier.apply(mode);
		
		int hoverColour = 0x20000000;
		int buttonColour = 0x40000000;
		
		boolean hoveringLeftCycleButton = RenderUtils.isMouseHovering(parent, mouseX, mouseY, x, y, x + 20, y + 14);
		boolean hoveringRightCycleButton = RenderUtils.isMouseHovering(parent, mouseX, mouseY, x + 44 + longestModeName, y, x + 64 + longestModeName, y + 14);
		
		RenderUtils.fill(parent, context, x, y, x + 20, y + 14, hoveringLeftCycleButton ? hoverColour : buttonColour);
		RenderUtils.renderScaledText(parent, context, Text.literal("<"), x + 10 - parent.getTextRenderer().getWidth("<") / 2, y + 7 - parent.getTextRenderer().fontHeight / 2, 0xFFFFFFFF, 1f);
	
		MutableText displayText = Text.empty().equals(iconText) ? Text.empty().append(modeName) : Text.empty().append(iconText).append(" ").append(modeName);
		RenderUtils.fill(parent, context, x + 22, y, x + 42 + longestModeName, y + 14, buttonColour);
		RenderUtils.renderScaledText(parent, context, displayText, x + 32 + longestModeName / 2 - parent.getTextRenderer().getWidth(displayText) / 2, y + 7 - parent.getTextRenderer().fontHeight / 2, 0xFFFFFFFF, 1f);
		
		RenderUtils.fill(parent, context, x + 44 + longestModeName, y, x + 64 + longestModeName, y + 14, hoveringRightCycleButton ? hoverColour : buttonColour);
		RenderUtils.renderScaledText(parent, context, Text.literal(">"), x + 54 + longestModeName - parent.getTextRenderer().getWidth(">") / 2, y + 7 - parent.getTextRenderer().fontHeight / 2, 0xFFFFFFFF, 1f);
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (RenderUtils.isMouseHovering(parent, mouseX, mouseY, x, y, x + 20, y + 14) && button == GLFW.GLFW_MOUSE_BUTTON_1) {
			int currentIndex = modes.indexOf(mode);
			int index = currentIndex == 0 ? modes.size() - 1 : currentIndex - 1;
			mode = modes.get(index);
		} else if (RenderUtils.isMouseHovering(parent, mouseX, mouseY, x + 44 + longestModeName, y, x + 64 + longestModeName, y + 14)) {
			int currentIndex = modes.indexOf(mode);
			int index = currentIndex == modes.size() - 1 ? 0 : currentIndex + 1;
			mode = modes.get(index);
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {}

	@Override
	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {}
}
