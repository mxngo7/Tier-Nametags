package me.mxngo.ui.components;

import net.minecraft.client.gui.DrawContext;

public abstract class TabContentComponent {
	public abstract void tick();
	public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);
	public abstract void mouseClicked(double mouseX, double mouseY, int button);
}