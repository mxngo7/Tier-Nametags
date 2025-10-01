package me.mxngo.ocetiers;

import static java.util.Map.entry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class OceTiersAPIWrapper {
	private static final String API_URL = "https://ocetiers.net/api";
	private static final HttpClient httpClient = HttpClient.newHttpClient();
	private static final Gson gson = new Gson();
	
	private static String getAPIUrl(Endpoint endpoint) {
		return API_URL.concat(endpoint.getPath());
	}
	
	private static CompletableFuture<HttpResponse<String>> fetch(Endpoint endpoint) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(getAPIUrl(endpoint))).GET().build();
		return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
	}
	
	private static TieredPlayer[] formatPlayerlist(String body) {
		ArrayList<TieredPlayer> players = new ArrayList<>();
		
		PlayerResponse[] data = gson.fromJson(body, PlayerResponse[].class);
		
		Map<String, Gamemode> gamemodeMap = Map.ofEntries(
			entry("sword", Gamemode.SWORD),
			entry("diamondPot", Gamemode.DIAMOND_POT),
			entry("netheritePot", Gamemode.NETHERITE_POT),
			entry("axe", Gamemode.AXE),
			entry("uhc", Gamemode.UHC),
			entry("smp", Gamemode.SMP),
			entry("crystal", Gamemode.CRYSTAL),
			entry("diamondSmp", Gamemode.DIAMOND_SMP),
			entry("cart", Gamemode.CART),
			entry("mace", Gamemode.MACE)
		);
		
		for (PlayerResponse playerResponse : data) {
			long[] tierData = new long[2];
			
			Map<String, GamemodeResponse> gamemodes = playerResponse.gamemodes();
			
			for (String gamemodeName : gamemodes.keySet()) {
				GamemodeResponse gamemodeResponse = gamemodes.get(gamemodeName);
				Gamemode gamemode = gamemodeMap.get(gamemodeName);
				Tier tier = Tier.fromName(gamemodeResponse.tier());
				
				int i = (gamemode == Gamemode.CART || gamemode == Gamemode.MACE) ? 1 : 0;
				tierData[i] = tier.applyTo(gamemode, tierData[i]);
			}
			
			long metadata = tierData[1];
			metadata = (metadata & ~(0xFFFFL << 16)) | ((long) playerResponse.leaderboardPosition() << 16);
			metadata = (metadata & ~(0xFFFFL << 32)) | ((long) playerResponse.leaderboardPoints() << 32);
			tierData[1] = metadata;
			
			players.add(new TieredPlayer(playerResponse.username(), tierData));
		}
		
		return players.toArray(new TieredPlayer[0]);
	}
	
	public static CompletableFuture<TieredPlayer[]> getPlayers() {
		CompletableFuture<HttpResponse<String>> httpResponse = fetch(Endpoint.LEADERBOARD);
		CompletableFuture<TieredPlayer[]> playerList = httpResponse
			.thenApply(HttpResponse::body)
			.thenApply(OceTiersAPIWrapper::formatPlayerlist);
		
		return playerList;
	}
	
	private enum Endpoint {
		LEADERBOARD("/leaderboard?full=true");
		
		private String path;
		
		private Endpoint(String path) {
			this.path = path;
		}
		
		public String getPath() {
			return this.path;
		}
	}
	
	private record GamemodeResponse(@SerializedName("tier") String tier) {}
	
	private record PlayerResponse(
		@SerializedName("username") String username,
		@SerializedName("leaderboardPosition") int leaderboardPosition,
		@SerializedName("score") int leaderboardPoints,
		@SerializedName("gameModes") Map<String, GamemodeResponse> gamemodes
	) {}
	
}
