package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.DrawContext.ScissorStack;
import net.minecraft.client.gui.render.state.GuiRenderState;

@Mixin(DrawContext.class)
public interface IDrawContextAccessor {
	@Accessor("state")
	public GuiRenderState getState();
	
	@Accessor("scissorStack")
	public ScissorStack getScissorStack();
}
