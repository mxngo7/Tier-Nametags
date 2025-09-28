package me.mxngo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public interface IMinecraftClientAccessor {
	@Accessor("authenticationService")
	public YggdrasilAuthenticationService getAuthenticationService();
}
