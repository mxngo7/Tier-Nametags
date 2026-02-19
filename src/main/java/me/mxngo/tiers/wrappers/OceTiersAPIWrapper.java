package me.mxngo.tiers.wrappers;

import static java.util.Map.entry;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.annotations.SerializedName;

import me.mxngo.tiers.APIWrapper;
import me.mxngo.tiers.Gamemode;
import me.mxngo.tiers.Tier;
import me.mxngo.tiers.TieredPlayer;

public class OceTiersAPIWrapper extends APIWrapper {
	@Override
	public String getApiUrl() {
		return "https://ocetiers.net/api";
	}
	
	private TieredPlayer[] parsePlayerlist(String body) {
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
	
	public CompletableFuture<TieredPlayer[]> getPlayers() {
		CompletableFuture<HttpResponse<String>> httpResponse = fetch(Endpoint.LEADERBOARD);
		CompletableFuture<TieredPlayer[]> playerList = httpResponse
			.thenApply(HttpResponse::body)
			.thenApply(this::parsePlayerlist);
		
		return playerList;
	}
	
	private static enum Endpoint implements APIEndpoint {
		LEADERBOARD("/leaderboard?full=true");
		
		private final String path;
		
		private Endpoint(String path) {
			this.path = path;
		}
		
		public String getPath(String... args) {
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
