package me.mxngo.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import me.mxngo.ocetiers.SkinCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
	private MinecraftClient mc = MinecraftClient.getInstance();
	
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/world/ClientWorld;addEntity(Lnet/minecraft/entity/Entity;)V")
	public void addEntity(Entity entity, CallbackInfo info) {
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			GameProfile profile = player.getGameProfile();
			
			Supplier<SkinTextures> textureSupplier = mc.getSkinProvider().getSkinTexturesSupplier(profile);
			SkinCache.cachePlayer(profile.getName(), textureSupplier);
		}
	}
}