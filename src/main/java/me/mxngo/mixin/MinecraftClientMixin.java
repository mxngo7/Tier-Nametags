package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.mxngo.TierNametags;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	private TierNametags instance = TierNametags.getInstance();
	
	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/MinecraftClient;tick()V")
	public void tick(CallbackInfo info) {
		instance.tick();
	}
}
