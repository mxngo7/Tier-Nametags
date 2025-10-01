package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import me.mxngo.TierNametags;
import me.mxngo.config.TierNametagsConfig;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
	private TierNametags instance = TierNametags.getInstance();
	
	@Inject(at = @At("RETURN"), method = "Lnet/minecraft/client/gui/hud/PlayerListHud;getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;", cancellable = true)
	public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> info) {
		TierNametagsConfig config = instance.getConfig();
		if (!config.showOnTablist) return;
		
		GameProfile profile = entry.getProfile();
		String name = profile.getName();
		Text displayName = entry.getDisplayName();
		if (displayName == null) displayName = Text.literal(name);
		
		MutableText component = instance.getNametagComponent(name);
		if (component == null) return;
		
		info.setReturnValue(instance.applyTierToDisplayName(name, (MutableText) displayName, component));
	}
}