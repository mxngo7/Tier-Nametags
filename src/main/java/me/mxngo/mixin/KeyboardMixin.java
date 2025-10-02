package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.mxngo.TierNametags;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;

@Mixin(Keyboard.class)
public class KeyboardMixin {
	private TierNametags instance = TierNametags.getInstance();
	
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Keyboard;onKey(JILnet/minecraft/client/input/KeyInput;)V")
	public void onKey(long window, int action, KeyInput keyInput, CallbackInfo info) {
		int key = keyInput.key();
		int scancode = keyInput.scancode();
		int modifiers = keyInput.modifiers();
		
		instance.onKey(window, key, scancode, action, modifiers);
	}
}
