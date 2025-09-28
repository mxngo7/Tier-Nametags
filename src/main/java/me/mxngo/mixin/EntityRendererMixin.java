package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import me.mxngo.TierNametags;
import me.mxngo.config.TierNametagsConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
	private TierNametags instance = TierNametags.getInstance();
	
	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/render/entity/EntityRenderer;updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V")
	public void updateRenderState(T entity, S state, float d, CallbackInfo info) {
		TierNametagsConfig config = instance.getConfig();
		
		if (!config.showOnNametags) return;
		if (!(entity instanceof PlayerEntity)) return;
		
		AbstractClientPlayerEntity playerEntity = (AbstractClientPlayerEntity) entity;
		GameProfile profile = playerEntity.getGameProfile();
		String name = profile.getName();
		
		MutableText updatedDisplayName = instance.getDisplayName(name);
		if (updatedDisplayName != null) state.displayName = updatedDisplayName;
	}
}