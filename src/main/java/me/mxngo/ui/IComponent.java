package me.mxngo.ui;

import net.minecraft.client.gui.DrawContext;

public interface IComponent {
	public void tick();
	public void render(DrawContext context, int mouseX, int mouseY, float delta);
	public void mouseClicked(double mouseX, double mouseY, int button);
	public void mouseReleased(double mouseX, double mouseY, int button);
	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
}
