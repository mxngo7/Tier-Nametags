package me.mxngo.ui.util;

import me.mxngo.TierNametags;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RenderUtils {
//	private static final MinecraftClient mc = MinecraftClient.getInstance();
//	private static MappableRingBuffer vertexBuffer;
	
	public static final int iX = 854;
	public static final int iY = 480;
	
	public static int getScaled(int x, int a, int b) {
		return (int) (((float) x / a) * b);
	}
	
	public static int getScaled(float x, int a, int b) {
		return (int) ((x / a) * b);
	}
	
	public static double easeOut(double x, int exp) {
		return 1 - Math.pow(1 - x, exp);
	}
	
//	public static void testFillF(DrawContext context, float x1, float y1, float x2, float y2) {
//		// fix memory leak lol
//		
//		if (x1 < x2) {
//			float i = x1;
//			x1 = x2;
//			x2 = i;
//		}
//
//		if (y1 < y2) {
//			float i = y1;
//			y1 = y2;
//			y2 = i;
//		}
//		
//		RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
//		VertexFormat vertexFormat = VertexFormats.POSITION_COLOR;
//		
//		Tessellator tessellator = Tessellator.getInstance();
//		BufferBuilder buffer = tessellator.begin(DrawMode.TRIANGLE_STRIP, vertexFormat);
//		
//		buffer.vertex(context.getMatrices(), x1, y1, 0).color(0xFFFFFFFF);
//		buffer.vertex(context.getMatrices(), x2, y1, 0).color(0xFFFFFFFF);
//		buffer.vertex(context.getMatrices(), x2, y2, 0).color(0xFFFFFFFF);
//		buffer.vertex(context.getMatrices(), x1, y2, 0).color(0xFFFFFFFF);
//		
//		BuiltBuffer builtBuffer = buffer.end();
//		BuiltBuffer.DrawParameters drawParameters = builtBuffer.getDrawParameters();
//		
//		int vertexBufferSize = drawParameters.vertexCount() * vertexFormat.getVertexSize();
//
//		if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
//			vertexBuffer = new MappableRingBuffer(() -> TierNametags.MODID + ".RenderUtils.fill()", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
//		}
//
//		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
//
//		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.getBlocking().slice(0, builtBuffer.getBuffer().remaining()), false, true)) {
//			MemoryUtil.memCopy(builtBuffer.getBuffer(), mappedView.data());
//		}
//
//		GpuBuffer vertices = vertexBuffer.getBlocking();
//		
//		GpuBuffer indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.getSortedBuffer());
//		VertexFormat.IndexType indexType = drawParameters.indexType();
//		
//		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), new Vector4f(1f, 1f, 1f, 1f), RenderSystem.getModelOffset(), RenderSystem.getTextureMatrix(), 1f);
//		
//		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> TierNametags.MODID + ".RenderUtils.fill()", mc.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), mc.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty())) {
//			renderPass.setPipeline(pipeline);
//			RenderSystem.bindDefaultUniforms(renderPass);
//			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
//			renderPass.setVertexBuffer(0, vertices);
//			renderPass.setIndexBuffer(indices, indexType);
//			renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
//			renderPass.close();
//		}
//		
//		builtBuffer.close();
//	}
	
	public static void enableScissor(Screen screen, DrawContext context, int x1, int y1, int x2, int y2) {
		context.enableScissor(getScaled(x1, iX, screen.width), getScaled(y1, iY, screen.height), getScaled(x2, iX, screen.width), getScaled(y2, iY, screen.height));
	}
	
	public static void fill(Screen screen, DrawContext context, int x1, int y1, int x2, int y2, int colour) {
		context.fill(getScaled(x1, iX, screen.width), getScaled(y1, iY, screen.height), getScaled(x2, iX, screen.width), getScaled(y2, iY, screen.height), colour);
	}
	
	public static void fillGradient(Screen screen, DrawContext context, int x1, int y1, int x2, int y2, int startColour, int stopColour) {
		context.fillGradient(getScaled(x1, iX, screen.width), getScaled(y1, iY, screen.height), getScaled(x2, iX, screen.width), getScaled(y2, iY, screen.height), startColour, stopColour);
	}
	
	public static void renderScaledText(Screen screen, DrawContext context, Text text, int x, int y, int colour, float scale, boolean shadow) {
		scale *= Math.min((float) screen.width / iX, (float) screen.height / iY);
		context.getMatrices().pushMatrix();
		context.getMatrices().scale(scale);
		context.drawText(screen.getTextRenderer(), text, getScaled((int) (x / scale), iX, screen.width), getScaled((int) (y / scale), iY, screen.height), colour, shadow);
		context.getMatrices().popMatrix();
	}
	
	public static void renderScaledText(Screen screen, DrawContext context, Text text, int x, int y, int colour, float scale) {
		renderScaledText(screen, context, text, x, y, colour, scale, true);
	}
	
	public static void renderScaledTextWithGradient(Screen screen, DrawContext context, Text text, int x, int y, int startColour, int stopColour, float scale, boolean shadow) {
		int[] gradient = TierNametags.createGradient(startColour, stopColour, text.getString().length());
		MutableText newText = Text.empty();
		
		for (int i = 0; i < text.getString().length(); i++) {
			newText.append(Text.literal(String.valueOf(text.getString().charAt(i))).withColor(gradient[i]));
		}
		renderScaledText(screen, context, newText, x, y, 0xFFFFFFFF, scale, shadow);
	}
	
	public static void renderScaledTextWithGradient(Screen screen, DrawContext context, Text text, int x, int y, int startColour, int stopColour, float scale) {
		renderScaledTextWithGradient(screen, context, text, x, y, startColour, stopColour, scale, true);
	}
	
	public static void renderBorder(Screen screen, DrawContext context, int x, int y, int width, int height, int colour, int size) {
		int scaledSizeX = Math.max(1, getScaled(size, iX, screen.width));
	    int scaledSizeY = Math.max(1, getScaled(size, iY, screen.height));

	    int left = getScaled(x, iX, screen.width);
	    int top = getScaled(y, iY, screen.height);
	    int right = getScaled(x + width, iX, screen.width);
	    int bottom = getScaled(y + height, iY, screen.height);

	    context.fill(left, top, right, top + scaledSizeY, colour);
	    context.fill(left, bottom - scaledSizeY, right, bottom, colour);
	    context.fill(left, top + scaledSizeY, left + scaledSizeX, bottom - scaledSizeY, colour);
	    context.fill(right - scaledSizeX, top + scaledSizeY, right, bottom - scaledSizeY, colour);
	}
	
	public static void renderBorder(Screen screen, DrawContext context, int x, int y, int width, int height, int colour) {
		renderBorder(screen, context, x, y, width, height, colour, 1);
	}
	
	public static void renderGradientBorder(Screen screen, DrawContext context, int x, int y, int width, int height, int startColour, int stopColour, int size) {
		int scaledSizeX = Math.max(1, getScaled(size, iX, screen.width));
	    int scaledSizeY = Math.max(1, getScaled(size, iY, screen.height));

	    int left = getScaled(x, iX, screen.width);
	    int top = getScaled(y, iY, screen.height);
	    int right = getScaled(x + width, iX, screen.width);
	    int bottom = getScaled(y + height, iY, screen.height);

	    context.fill(left, top, right, top + scaledSizeY, startColour);
	    context.fill(left, bottom - scaledSizeY, right, bottom, stopColour);
	    context.fillGradient(left, top + scaledSizeY, left + scaledSizeX, bottom - scaledSizeY, startColour, stopColour);
	    context.fillGradient(right - scaledSizeX, top + scaledSizeY, right, bottom - scaledSizeY, startColour, stopColour);
	}
	
	public static void renderGradientBorder(Screen screen, DrawContext context, int x, int y, int width, int height, int startColour, int stopColour) {
		renderGradientBorder(screen, context, x, y, width, height, startColour, stopColour, 1);
	}
	
	public static void renderTexture(Screen screen, DrawContext context, Identifier identifier, int x, int y, int width, int height) {
		width = getScaled(width, iX, screen.width);
		height = getScaled(height, iY, screen.height);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, identifier, getScaled(x, iX, screen.width), getScaled(y, iY, screen.height), 0, 0, width, height, width, height);
	}
	
	public static void renderTexture(Screen screen, DrawContext context, Identifier identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		width = getScaled(width, iX, screen.width);
		height = getScaled(height, iY, screen.height);
		textureWidth = getScaled(textureWidth, iX, screen.width);
		textureHeight = getScaled(textureHeight, iY, screen.height);
		u = getScaled(u, iX, screen.width);
		v = getScaled(v, iY, screen.height);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, identifier, getScaled(x, iX, screen.width), getScaled(y, iY, screen.height), u, v, width, height, textureWidth, textureHeight);
	}
	
	public static boolean isMouseHovering(Screen screen, double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
		return mouseX >= getScaled(x1, iX, screen.width) && mouseX <= getScaled(x2, iX, screen.width) && mouseY >= getScaled(y1, iY, screen.height) && mouseY <= getScaled(y2, iY, screen.height);
	}
}
