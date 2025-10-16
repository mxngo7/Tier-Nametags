package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.mxngo.TierNametags;
import net.minecraft.client.Keyboard;

@Mixin(Keyboard.class)
public class KeyboardMixin {
	private TierNametags instance = TierNametags.getInstance();
	
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Keyboard;onKey(JIIII)V")
	public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
		instance.onKey(window, key, scancode, action, modifiers);
	}
}
