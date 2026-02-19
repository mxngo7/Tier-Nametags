package me.mxngo.tiers.wrappers;

import static java.util.Map.entry;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import me.mxngo.config.Tierlist;
import me.mxngo.tiers.APIWrapper;
import me.mxngo.tiers.Gamemode;
import me.mxngo.tiers.Leaderboard;
import me.mxngo.tiers.Leaderboard.LeaderboardEntry;
import me.mxngo.tiers.Tier;
import me.mxngo.tiers.TieredPlayer;
import me.mxngo.tiers.TierlistManager;

public class MCTiersAPIWrapper extends APIWrapper {
	@Override
	public String getApiUrl() {
		return "https://mctiers.com/api/v2";
	}
	
	private final ConcurrentHashMap<Endpoint, Boolean> inProgressRequests = new ConcurrentHashMap<>();
	
	private final Type GamemodeResponseType = new TypeToken<Map<String, List<PlayerResponseFromGamemode>>>() {}.getType();
	
	private int overallOffset = 0;
	private final HashMap<Gamemode, Integer> gamemodeOffsets = new HashMap<>();
	
	private final Leaderboard leaderboard;
	
	private final Map<String, Gamemode> gamemodeMap = Map.ofEntries(
		entry("sword", Gamemode.SWORD),
		entry("pot", Gamemode.DIAMOND_POT),
		entry("nethop", Gamemode.NETHERITE_POT),
		entry("axe", Gamemode.AXE),
		entry("uhc", Gamemode.UHC),
		entry("smp", Gamemode.SMP),
		entry("vanilla", Gamemode.CRYSTAL),
		entry("mace", Gamemode.MACE)
	);
	
	public MCTiersAPIWrapper(TierlistManager manager) {
		 leaderboard = manager.getLeaderboard(Tierlist.MCTIERS);
	}
	
	private TieredPlayer[] parsePlayerlist(String body) {
		ArrayList<TieredPlayer> players = new ArrayList<>();
		
		PlayerResponse[] data = gson.fromJson(body, PlayerResponse[].class);
		
		for (PlayerResponse playerResponse : data) {
			long[] tierData = new long[2];
			
			Map<String, GamemodeDataResponse> gamemodes = playerResponse.gamemodes();
			
			for (String gamemodeName : gamemodes.keySet()) {
				GamemodeDataResponse gamemodeResponse = gamemodes.get(gamemodeName);
				Gamemode gamemode = gamemodeMap.get(gamemodeName);
				byte tierPos = gamemodeResponse.pos() == 0 ? Tier.HT.getValue() : Tier.LT.getValue();
				byte retired = gamemodeResponse.retired() ? Tier.R.getValue() : 0;
				Tier tier = Tier.fromValue((byte) (retired | tierPos | gamemodeResponse.tier()));
				
				int i = (gamemode == Gamemode.CART || gamemode == Gamemode.MACE) ? 1 : 0;
				tierData[i] = tier.applyTo(gamemode, tierData[i]);
			}
			
			long metadata = tierData[1];
			metadata = (metadata & ~(0xFFFFL << 16)) | ((long) (players.size() + 1) << 16);
			metadata = (metadata & ~(0xFFFFL << 32)) | ((long) playerResponse.leaderboardPoints() << 32);
			tierData[1] = metadata;
			
			players.add(new TieredPlayer(playerResponse.username(), tierData));
		}
		
		inProgressRequests.put(Endpoint.OVERALL, false);
		return players.toArray(new TieredPlayer[0]);
	}
	
	private TieredPlayer[] parseGamemodePlayerlist(String body, Gamemode gamemode) {
		ArrayList<TieredPlayer> players = new ArrayList<>();
		
		Map<String, List<PlayerResponseFromGamemode>> data = gson.fromJson(body, GamemodeResponseType);
		
		for (String key : data.keySet()) {
			for (PlayerResponseFromGamemode player : data.get(key)) {
				long[] tierData = new long[2];
				byte tierPos = player.pos == 0 ? Tier.HT.getValue() : Tier.LT.getValue();
				Tier tier = Tier.fromValue((byte) (tierPos | Integer.parseInt(key)));
				
				int i = (gamemode == Gamemode.CART || gamemode == Gamemode.MACE) ? 1 : 0;
				tierData[i] = tier.applyTo(gamemode, tierData[i]);
				players.add(new TieredPlayer(player.username(), tierData));
			}
		}
		
		inProgressRequests.put(gamemodeToEndpoint(gamemode), false);
		return players.toArray(new TieredPlayer[0]);
	}
	
	private TieredPlayer parsePlayerProfile(String body) {
		ProfileResponse data = gson.fromJson(body, ProfileResponse.class);
		
		long[] tierData = new long[2];
		Map<String, GamemodeDataResponse> gamemodes = data.gamemodes();
		
		for (String gamemodeName : gamemodes.keySet()) {
			GamemodeDataResponse gamemodeResponse = gamemodes.get(gamemodeName);
			Gamemode gamemode = gamemodeMap.get(gamemodeName);
			byte tierPos = gamemodeResponse.pos() == 0 ? Tier.HT.getValue() : Tier.LT.getValue();
			byte retired = gamemodeResponse.retired() ? Tier.R.getValue() : 0;
			Tier tier = Tier.fromValue((byte) (retired | tierPos | gamemodeResponse.tier()));
			int i = (gamemode == Gamemode.CART || gamemode == Gamemode.MACE) ? 1 : 0;
			tierData[i] = tier.applyTo(gamemode, tierData[i]);
		}
		
		long metadata = tierData[1];
		metadata = (metadata & ~(0xFFFFL << 16)) | ((long) data.leaderboardPosition() << 16);
		metadata = (metadata & ~(0xFFFFL << 32)) | ((long) data.leaderboardPoints() << 32);
		tierData[1] = metadata;
		
		inProgressRequests.put(Endpoint.PROFILE_BY_NAME, false);
		return new TieredPlayer(data.username(), tierData);
	}
	
	public CompletableFuture<TieredPlayer[]> getPlayers(int from, Gamemode gamemode) {
		if (inProgressRequests.getOrDefault(gamemodeToEndpoint(gamemode), false))
			return CompletableFuture.completedFuture(new TieredPlayer[] {});
		
		inProgressRequests.put(gamemodeToEndpoint(gamemode), true);
		
		if (gamemode == null)
			overallOffset += 50;
		else
			gamemodeOffsets.put(gamemode, gamemodeOffsets.getOrDefault(gamemode, 0) + 50);
		
		CompletableFuture<HttpResponse<String>> httpResponse = fetch(gamemodeToEndpoint(gamemode), String.valueOf(from));
		CompletableFuture<TieredPlayer[]> playerList = httpResponse
			.thenApply(HttpResponse::body)
			.thenApply(body -> {
				if (gamemode == null)
					return this.parsePlayerlist(body);
				else
					return this.parseGamemodePlayerlist(body, gamemode);
			});
		
		return playerList;
	}
	
	public CompletableFuture<TieredPlayer[]> getPlayers() {
		return getPlayers(0, null);
	}
	
	public CompletableFuture<TieredPlayer[]> getPlayers(int from) {
		return getPlayers(from, null);
	}
	
	public CompletableFuture<TieredPlayer[]> getPlayers(Gamemode gamemode) {
		return getPlayers(0, gamemode);
	}
	
	public CompletableFuture<TieredPlayer> getPlayer(String name) {
		if (inProgressRequests.getOrDefault(Endpoint.PROFILE_BY_NAME, false))
			return CompletableFuture.completedFuture(null);
		
		LeaderboardEntry entry = leaderboard.getEntry(name);
		if (entry != null && entry.state().isHydrated())
			return CompletableFuture.completedFuture(entry.player());
		
		inProgressRequests.put(Endpoint.PROFILE_BY_NAME, true);
		
		CompletableFuture<HttpResponse<String>> httpResponse = fetch(Endpoint.PROFILE_BY_NAME, name);
		CompletableFuture<TieredPlayer> player = httpResponse
			.thenApply(HttpResponse::body)
			.thenApply(this::parsePlayerProfile);
		
		return player;
	}
	
	private Endpoint gamemodeToEndpoint(Gamemode gamemode) {
		if (gamemode == null) return Endpoint.OVERALL;
		return switch (gamemode) {
			case Gamemode.SWORD -> Endpoint.SWORD;
			case Gamemode.DIAMOND_POT -> Endpoint.POT;
			case Gamemode.NETHERITE_POT -> Endpoint.NETHOP;
			case Gamemode.AXE -> Endpoint.AXE;
			case Gamemode.UHC -> Endpoint.UHC;
			case Gamemode.SMP -> Endpoint.SMP;
			case Gamemode.CRYSTAL -> Endpoint.VANILLA;
			case Gamemode.MACE -> Endpoint.MACE;
			default -> Endpoint.OVERALL; 
		};
	}
	
	public int getOffset(Gamemode gamemode) {
		return gamemode == null ? overallOffset : gamemodeOffsets.getOrDefault(gamemode, 0);
	}
	
	public void resetOffsets() {
		overallOffset = 0;
		gamemodeOffsets.clear();
	}
	
	private static enum Endpoint implements APIEndpoint {
		OVERALL("/mode/overall?from=%s&count=50"),
		SWORD("/mode/sword?from=%s&count=50"),
		POT("/mode/pot?from=%s&count=50"),
		NETHOP("/mode/nethop?from=%s&count=50"),
		AXE("/mode/axe?from=%s&count=50"),
		UHC("/mode/uhc?from=%s&count=50"),
		SMP("/mode/smp?from=%s&count=50"),
		VANILLA("/mode/vanilla?from=%s&count=50"),
		MACE("/mode/mace?from=%s&count=50"),
		
		PROFILE_BY_NAME("/profile/by-name/%s");
		
		private final String path;
		
		private Endpoint(String path) {
			this.path = path;
		}
		
		public String getPath(String... args) {
			if (args.length == 0) return this.path;
			else return String.format(this.path, (Object[]) args);
		}
	}
	
	private record GamemodeDataResponse(
		@SerializedName("tier") byte tier,
		@SerializedName("pos") byte pos,
		@SerializedName("peak_tier") Byte peakTier,
		@SerializedName("peak_pos") Byte peakPos,
		@SerializedName("retired") boolean retired
	) {}
	
	private record PlayerResponse(
		@SerializedName("uuid") String uuid,
		@SerializedName("name") String username,
		@SerializedName("region") String region,
		@SerializedName("points") int leaderboardPoints,
		@SerializedName("rankings") Map<String, GamemodeDataResponse> gamemodes
	) {}
	
	private record PlayerResponseFromGamemode(
		@SerializedName("uuid") String uuid,
		@SerializedName("name") String username,
		@SerializedName("region") String region,
		@SerializedName("pos") int pos
	) {}
	
	private record ProfileResponse(
		@SerializedName("uuid") String uuid,
		@SerializedName("name") String username,
		@SerializedName("region") String region,
		@SerializedName("points") int leaderboardPoints,
		@SerializedName("overall") int leaderboardPosition,
		@SerializedName("rankings") Map<String, GamemodeDataResponse> gamemodes
	) {}
}
