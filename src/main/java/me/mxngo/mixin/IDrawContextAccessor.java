package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.DrawContext;

@Mixin(DrawContext.class)
public interface IDrawContextAccessor {
//	@Accessor("state")
//	public GuiRenderState getState();
//	
//	@Accessor("scissorStack")
//	public ScissorStack getScissorStack();
}
