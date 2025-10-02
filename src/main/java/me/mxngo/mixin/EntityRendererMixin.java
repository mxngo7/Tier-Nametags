package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.mojang.authlib.GameProfile;

import me.mxngo.TierNametags;
import me.mxngo.config.TierNametagsConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
	private TierNametags instance = TierNametags.getInstance();
	
	@ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V"), method = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void renderLabelIfPresent(Args args) {
		TierNametagsConfig config = instance.getConfig();
		
		Entity entity = args.get(0);
		
		if (entity instanceof AbstractClientPlayerEntity && !entity.shouldRenderName()) {
			args.set(1, Text.empty());
			return;
		}
		
		if (!config.showOnNametags) return;
		if (!(entity instanceof PlayerEntity)) return;
		
		AbstractClientPlayerEntity playerEntity = (AbstractClientPlayerEntity) entity;
		GameProfile profile = playerEntity.getGameProfile();
		String name = profile.getName();
		
		MutableText displayName = (MutableText) entity.getDisplayName();
		if (displayName == null) displayName = Text.literal(name);
		
		MutableText component = instance.getNametagComponent(name);
		if (component == null) return;
		
		args.set(1, instance.applyTierToDisplayName(name, displayName, component));
	}
	
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V", cancellable = true)
	public void renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, float delta, CallbackInfo info) {
		if (text.getString().isEmpty()) info.cancel();
	}
}