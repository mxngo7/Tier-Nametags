package me.mxngo;

import java.util.HashMap;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class SkinCache {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static HashMap<String, Supplier<SkinTextures>> cache = new HashMap<>();
	private static HashMap<String, GameProfile> profileCache = new HashMap<>();
	
	public static void clear() {
		cache.clear();
	}
	
	public static void cacheWorld(ClientWorld world) {
		for (AbstractClientPlayerEntity player : world.getPlayers()) cachePlayer(player);
	}
	
	public static void cachePlayer(AbstractClientPlayerEntity player) {
		cache.put(player.getGameProfile().getName().toLowerCase(), mc.getSkinProvider().getSkinTexturesSupplier(player.getGameProfile()));
	}
	
	public static void cachePlayer(PlayerEntity player) {
		cachePlayer(player.getGameProfile());
	}
	
	public static void cachePlayer(GameProfile profile) {
		cache.put(profile.getName().toLowerCase(), mc.getSkinProvider().getSkinTexturesSupplier(profile));
		profileCache.put(profile.getName().toLowerCase(), profile);
	}
	
	public static void cachePlayer(String name, Supplier<SkinTextures> textureSupplier) {
		cache.put(name.toLowerCase(), textureSupplier);
	}
	
	public static void cacheProfile(GameProfile profile) {
		profileCache.put(profile.getName().toLowerCase(), profile);
	}
	
	private static SkinTextures getDelegateSkinTextures(String name) {
		GameProfile profile = getProfile(name);
		
		if (profile == null)
			return null; //mc.getSkinProvider().getSkinTextures(new GameProfile(UUID.randomUUID(), name));
		else return mc.getSkinProvider().getSkinTextures(profile);
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
		return getSkinTextures(profile.getName().toLowerCase());
	}
	
	public static SkinTextures getPlayer(PlayerEntity player) {
		return getPlayer(player.getGameProfile());
	}
	
	public static Supplier<SkinTextures> getSupplier(String name) {
		Supplier<SkinTextures> textureSupplier = cache.get(name.toLowerCase());
		
		if (textureSupplier != null)
			return textureSupplier;
		else return mc.getSkinProvider().getSkinTexturesSupplier(getProfile(name));
	}
	
	public static GameProfile getProfile(String name) {
		return profileCache.get(name.toLowerCase());
	}
	
	public static SkinTextures getUnknownSkinTextures() {
		return new SkinTextures(Identifier.of(TierNametags.MODID, "textures/entity/skin_unknown.png"), null, null, null, SkinTextures.Model.WIDE, true);
	}
	
	public static SkinTextures getUnknownFaceSkinTextures() {
		return new SkinTextures(Identifier.of(TierNametags.MODID, "textures/entity/skin_face_unknown.png"), null, null, null, SkinTextures.Model.WIDE, true);
	}
}
