package me.mxngo.ocetiers;

import java.util.HashMap;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;

import me.mxngo.TierNametags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;

public class SkinCache {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static HashMap<String, Supplier<SkinTextures>> cache = new HashMap<>();
	private static HashMap<String, GameProfile> profileCache = new HashMap<>();
	
	private static AssetInfo.TextureAsset skinUnknownAsset = new AssetInfo.TextureAsset() {
		@Override
		public Identifier id() {
			return Identifier.of(TierNametags.MODID);
		}

		@Override
		public Identifier texturePath() {
			return Identifier.of(TierNametags.MODID, "textures/entity/skin_unknown.png");
		}
	};
	
	public static void clear() {
		cache.clear();
	}
	
	public static void cacheWorld(ClientWorld world) {
		for (AbstractClientPlayerEntity player : world.getPlayers()) cachePlayer(player);
	}
	
	public static void cachePlayer(AbstractClientPlayerEntity player) {
		cache.put(player.getGameProfile().name().toLowerCase(), mc.getSkinProvider().supplySkinTextures(player.getGameProfile(), true));
	}
	
	public static void cachePlayer(PlayerEntity player) {
		cachePlayer(player.getGameProfile());
	}
	
	public static void cachePlayer(GameProfile profile) {
		cache.put(profile.name().toLowerCase(), mc.getSkinProvider().supplySkinTextures(profile, true));
		profileCache.put(profile.name().toLowerCase(), profile);
	}
	
	public static void cachePlayer(String name, Supplier<SkinTextures> textureSupplier) {
		cache.put(name.toLowerCase(), textureSupplier);
	}
	
	public static void cacheProfile(GameProfile profile) {
		profileCache.put(profile.name().toLowerCase(), profile);
	}
	
	private static SkinTextures getDelegateSkinTextures(String name) {
		GameProfile profile = getProfile(name);
		
		if (profile == null) return null;
		else return mc.getSkinProvider().supplySkinTextures(profile, true).get();
	}
	
	private static SkinTextures getSkinTextures(String name) {
		Supplier<SkinTextures> textureSupplier = cache.get(name.toLowerCase());
		
		if (textureSupplier != null)
			return textureSupplier.get();
		else return getDelegateSkinTextures(name);
	}
	
	public static SkinTextures getPlayer(String name) {
		return getSkinTextures(name);
	}
	
	public static SkinTextures getPlayer(GameProfile profile) {
		return getSkinTextures(profile.name().toLowerCase());
	}
	
	public static SkinTextures getPlayer(PlayerEntity player) {
		return getPlayer(player.getGameProfile());
	}
	
	public static Supplier<SkinTextures> getSupplier(String name) {
		Supplier<SkinTextures> textureSupplier = cache.get(name.toLowerCase());
		
		if (textureSupplier != null)
			return textureSupplier;
		else return mc.getSkinProvider().supplySkinTextures(getProfile(name), true);
	}
	
	public static GameProfile getProfile(String name) {
		return profileCache.get(name.toLowerCase());
	}
	
	public static SkinTextures getUnknownSkinTextures() {
		return new SkinTextures(skinUnknownAsset, null, null, PlayerSkinType.WIDE, true);
	}
}
