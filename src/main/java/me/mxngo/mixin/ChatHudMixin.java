package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import me.mxngo.TierNametags;
import me.mxngo.config.TierNametagsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	private TierNametags instance = TierNametags.getInstance();
	private MinecraftClient mc = MinecraftClient.getInstance();
	
	@ModifyVariable(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", argsOnly = true)
	private Text modifyMessage(Text message) {
		TierNametagsConfig config = instance.getConfig();
		
		if (!config.chat.enabled()) return message;
		if (mc.world == null) return message;
		
		for (PlayerListEntry player : mc.getNetworkHandler().getPlayerList()) {
			String name = player.getProfile().name();
			if (!message.getString().toLowerCase().contains(name.toLowerCase())) continue;
			
			MutableText result = Text.empty();

			for (Text child : message.getWithStyle(message.getStyle())) {
				String text = child.getString();
				int from = 0;
				int index;
				
				while ((index = text.toLowerCase().indexOf(name.toLowerCase(), from)) != -1) {
					if (index > from) result.append(Text.literal(text.substring(from, index)).setStyle(child.getStyle()));
					
					result.append(Text.literal(text.substring(index, index + name.length())).setStyle(child.getStyle()));
					
					from = index + name.length();
				}
				
				if (from < text.length()) result.append(Text.literal(text.substring(from)).setStyle(child.getStyle()));
			}
			
			MutableText component = instance.getNametagComponent(name, config.chat);
			if (component == null) continue;

			message = instance.applyTierToDisplayName(name, result, component, config.chat);
		}
		
		return message;
	}
}