package me.mxngo.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;

import net.minecraft.util.UserCache;

@Mixin(UserCache.class)
public interface IUserCacheInvoker {
	@Invoker("findProfileByName")
	public static Optional<GameProfile> findProfileByNameInvoker(GameProfileRepository repository, String name) {
		throw new AssertionError();
	};
}
