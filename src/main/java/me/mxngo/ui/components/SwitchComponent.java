package me.mxngo.ui.components;

import org.lwjgl.glfw.GLFW;

import me.mxngo.ui.IComponent;
import me.mxngo.ui.util.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SwitchComponent implements IComponent {
	private Screen parent;
	private Text label;
	private boolean toggled = false;
	private float deltaSinceToggled = 0f;
	private int u;
	
	private int switchOnColour, switchOffColour, handleOnColour, handleOffColour,
				switchOnHoverColour, switchOffHoverColour, handleOnHoverColour, handleOffHoverColour;
	
	public int x, y;
	
	public SwitchComponent(Screen parent, Text label, int x, int y, int u, int switchOnColour, int switchOffColour, int handleOnColour, int handleOffColour,
																		   int switchOnHoverColour, int switchOffHoverColour, int handleOnHoverColour, int handleOffHoverColour
	) {
		this.parent = parent;
		this.label = label;
		this.x = x;
		this.y = y;
		this.u = u;
		
		this.switchOnColour = switchOnColour;
		this.switchOffColour = switchOffColour;
		this.handleOnColour = handleOnColour;
		this.handleOffColour = handleOffColour;
		
		this.switchOnHoverColour = switchOnHoverColour;
		this.switchOffHoverColour = switchOffHoverColour;
		this.handleOnHoverColour = handleOnHoverColour;
		this.handleOffHoverColour = handleOffHoverColour;
	}
	
	public SwitchComponent(Screen parent, Text label, int x, int y, int u, int switchOnColour, int switchOffColour, int handleOnColour, int handleOffColour, 
			   							int switchOnHoverColour, int switchOffHoverColour, int handleOnHoverColour, int handleOffHoverColour, boolean toggled
	) {
		this.parent = parent;
		this.label = label;
		this.x = x;
		this.y = y;
		this.u = u;
		
		this.switchOnColour = switchOnColour;
		this.switchOffColour = switchOffColour;
		this.handleOnColour = handleOnColour;
		this.handleOffColour = handleOffColour;
		
		this.switchOnHoverColour = switchOnHoverColour;
		this.switchOffHoverColour = switchOffHoverColour;
		this.handleOnHoverColour = handleOnHoverColour;
		this.handleOffHoverColour = handleOffHoverColour;
		
		this.toggled = toggled;
	}
	
	public boolean isToggled() {
		return toggled;
	}
	
	public void setLabel(String label) {
		this.label = Text.literal(label);
	}
	
	@Override
	public void tick() {}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		RenderUtils.renderScaledText(parent, context, label, x + 27, y + (int) (parent.getTextRenderer().fontHeight / 2) - 3, 0xFFFFFFFF, 1f);

		int switchX = x;
		int switchY = y;
		int switchWidth = 21;
		int switchHeight = 11 + u;
		
		int handleX = toggled ? 11 : 1;
		int handleY = 1;
		int handleWidth = 9;
		int handleHeight = 9 + u;
		
		boolean hovering = RenderUtils.isMouseHovering(parent, mouseX, mouseY, switchX, switchY, switchX + switchWidth, switchY + switchHeight);
		
		int switchColour = hovering ? (toggled ? switchOnHoverColour : switchOffHoverColour) : (toggled ? switchOnColour : switchOffColour);
		int handleColour = hovering ? (toggled ? handleOnHoverColour : handleOffHoverColour) : (toggled ? handleOnColour : handleOffColour);
		
		RenderUtils.fill(parent, context, switchX, switchY, switchX + switchWidth, switchY + switchHeight, switchColour);
		RenderUtils.fill(parent, context, switchX + handleX, switchY + handleY, switchX + handleX + handleWidth, switchY + handleY + handleHeight, handleColour);
		
		deltaSinceToggled += delta;
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		int switchX = x;
		int switchY = y;
		int switchWidth = 21;
		int switchHeight = 11 + u;
		
		if (RenderUtils.isMouseHovering(parent, mouseX, mouseY, switchX, switchY, switchX + switchWidth, switchY + switchHeight) && button == GLFW.GLFW_MOUSE_BUTTON_1 && deltaSinceToggled > 2.5f) {
			toggled = !toggled;
			deltaSinceToggled = 0f;
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {}

	@Override
	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {}

}
