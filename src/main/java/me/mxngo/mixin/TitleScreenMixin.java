package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.mxngo.ui.screens.OutdatedVersionScreen;
import me.mxngo.update.VersionChecker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	private final MinecraftClient mc = MinecraftClient.getInstance();
	
	@Inject(at = @At("RETURN"), method = "init")
	public void titleScreenInit(CallbackInfo info) {
		if (VersionChecker.getLatestVersion().isEmpty()) return;
		
		if (VersionChecker.isCurrentVersionOutdated() && !VersionChecker.hasShownOutdatedVersionScreen()) {
			VersionChecker.setHasShownOutdatedVersionScreen(true);
			mc.setScreen(new OutdatedVersionScreen());
		}
	}
}
