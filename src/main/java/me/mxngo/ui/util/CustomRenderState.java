package me.mxngo.ui.util;

import java.util.function.BiConsumer;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import me.mxngo.mixin.IDrawContextAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;

public record CustomRenderState (
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		ScreenRect scissorArea,
		ScreenRect bounds,
		BiConsumer<VertexConsumer, Float> vertices
) implements SimpleGuiElementRenderState {
	public CustomRenderState(
		DrawContext context,
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		ScreenRect bounds,
		BiConsumer<VertexConsumer, Float> vertices
	) {
		this(pipeline, textureSetup, ((IDrawContextAccessor) context).getScissorStack().peekLast(), bounds, vertices);
	}
	
	@Override
	public void setupVertices(VertexConsumer vertices, float depth) {
		this.vertices.accept(vertices, depth);
	}
}