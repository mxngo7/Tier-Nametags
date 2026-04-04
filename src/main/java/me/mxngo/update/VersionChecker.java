package me.mxngo.update;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.gson.annotations.SerializedName;

import me.mxngo.TierNametags;
import me.mxngo.http.APIWrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

public class VersionChecker extends APIWrapper {
	private static Optional<String> latestVersion = Optional.empty();
	private static boolean shownOutdatedVersionScreen = false;
	
	@Override
	protected String getApiUrl() {
		return "https://api.modrinth.com/v2/project/tiernametags";
	}
	
	public static String getCurrentVersion() {
		return FabricLoader.getInstance().getModContainer(TierNametags.MODID)
		    .get()
		    .getMetadata()
		    .getVersion()
		    .getFriendlyString();
	}
	
	private static int compareVersions(String a, String b) {
		String[] partsA = a.split("\\.");
		String[] partsB = b.split("\\.");
		
		for (int i = 0; i < 3; i++) {
			int versionA = Integer.parseInt(partsA[i]);
			int versionB = Integer.parseInt(partsB[i]);
			
			if (versionA < versionB) return -1;
			if (versionA > versionB) return 1;
		}
		
		return 0;
	}
	
	public static boolean isCurrentVersionOutdated() {
		if (latestVersion.isEmpty() || latestVersion.get() == null) return false;
		int result = compareVersions(getCurrentVersion(), latestVersion.get().split("\\+")[0]);
		return result < 0;
	}
	
	private String parseVersion(String body) {
		String minecraftVersion = SharedConstants.getGameVersion().getName();
		
		VersionResponse[] data = gson.fromJson(body, VersionResponse[].class);
		VersionResponse latestVersionForCurrentMinecraftVersion = null;
		
		for (VersionResponse version : data) {
			if (Arrays.asList(version.gameVersions()).contains(minecraftVersion)) {
				latestVersionForCurrentMinecraftVersion = version;
				break;
			}
		}
		
		return (latestVersionForCurrentMinecraftVersion == null)
			? ""
			: latestVersionForCurrentMinecraftVersion.version();
	}
	
	public CompletableFuture<String> fetchLatestVersion() {
		CompletableFuture<HttpResponse<String>> httpResponse = fetch(Endpoint.VERSION);
		CompletableFuture<String> latestVersionResponse = httpResponse
			.thenApply(HttpResponse::body)
			.thenApply(this::parseVersion)
			.thenApply(version -> {
				latestVersion = Optional.ofNullable(version);
				return version;
			});
		
		return latestVersionResponse;
	}
	
	public static Optional<String> getLatestVersion() {
		return latestVersion;
	}
	
	public static boolean hasShownOutdatedVersionScreen() {
		return shownOutdatedVersionScreen;
	}
	
	public static void setHasShownOutdatedVersionScreen(boolean v) {
		shownOutdatedVersionScreen = v;
	}
	
	public static String getLatestVersionDownloadUrl() {
		return "https://modrinth.com/mod/tiernametags/version/" .concat(VersionChecker.getLatestVersion().get());
	}
	
	private static enum Endpoint implements APIEndpoint {
		VERSION("/version");

		private final String path;
		
		private Endpoint(String path) {
			this.path = path;
		}
		
		@Override
		public String getPath(String... args) {
			if (args.length == 0) return this.path;
			else return URLEncoder.encode(String.format(
				this.path, 
				(Object[]) args
			), StandardCharsets.UTF_8);
		}
	}

	private record VersionResponse(
		@SerializedName("game_versions") String[] gameVersions,
		@SerializedName("version_number") String version
	) {}
}
