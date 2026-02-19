package me.mxngo.tiers;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.mxngo.TierNametags;

public class Leaderboard {
	private Logger logger;
	private final ConcurrentHashMap<String, LeaderboardEntry> players = new ConcurrentHashMap<>();
	
	public Leaderboard(String name) {
		this.logger = LoggerFactory.getLogger(TierNametags.LOCALEMODID.concat("/").concat(name).concat("Leaderboard"));
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public List<LeaderboardEntry> getEntries() {
		return this.players.values().stream()
			.sorted(Comparator.comparingInt((LeaderboardEntry entry) -> {
				return entry.player().getLeaderboardPoints();
			}).reversed()
			.thenComparing(Comparator.comparingInt(
				(LeaderboardEntry entry) -> entry.player().getLeaderboardPoints()
			).reversed())
			.thenComparing(entry -> entry.player().name())
			).toList();
	}
	
	public List<TieredPlayer> getPlayers() {
		return getEntries().stream().map(entry -> entry.player()).toList();
	}
	
	public List<LeaderboardEntry> getEntries(Gamemode gamemode) {
		return this.players.values().stream()
			.filter(entry -> {
				TieredPlayer player = entry.player();
				Tier tier = player.getTier(gamemode);
				return !tier.isNone() && !tier.isRetired();
			}).sorted(Comparator.comparingInt((LeaderboardEntry entry) -> {
				return entry.player().getTier(gamemode).getTierValue();
			}).reversed()
			.thenComparing(Comparator.comparingInt(
				(LeaderboardEntry entry) -> entry.player().getLeaderboardPoints()
			).reversed())
			.thenComparing(entry -> entry.player().name())
			).toList();
	}
	
	public List<TieredPlayer> getPlayers(Gamemode gamemode) {
		return getEntries(gamemode).stream().map(entry -> entry.player()).toList();
	}
	
	public boolean playerExists(String name) {
		return players.containsKey(name);
	}
	
	public boolean playerExists(TieredPlayer player) {
		return playerExists(player.name());
	}
	
	public LeaderboardEntry getEntry(String name) {
		return players.get(name);
	}
	
	public LeaderboardEntry getEntry(TieredPlayer player) {
		return getEntry(player.name());
	}
	
	public HydrationState getState(TieredPlayer player) {
		if (!playerExists(player)) return HydrationState.NONE;
		LeaderboardEntry entry = getEntry(player);
		return entry.state();
	}
	
	public List<LeaderboardEntry> getTop(int end) {
		return getEntries().subList(0, Math.min(end, this.players.size()));
	}
	
	public TieredPlayer mergePartial(TieredPlayer incoming, Gamemode gamemode) {
		LeaderboardEntry entry = getEntry(incoming);
		if (entry == null) return incoming;
		return entry.player().with(gamemode, incoming.getTier(gamemode));
	}

	public void putPlayer(TieredPlayer incoming, HydrationState newState, Gamemode gamemode) {
		players.compute(incoming.name(), (name, existing) -> {
			if (existing == null) {
				if (newState.isPartial() && gamemode == null) {
					logger.error("Cannot update partial player data with a null gamemode: " + name);
					return null;
				}
				
				return new LeaderboardEntry(incoming, newState);
			}
			
			if (existing.state().isHydrated() && newState.isPartial())
				return existing;
			
			if (newState.isPartial()) {
				if (gamemode == null) {
					logger.error("Partial update requires a gamemode for: " + name);
					return existing;
				}
				
				TieredPlayer merged = mergePartial(incoming, gamemode);
				return new LeaderboardEntry(merged, existing.state());
			}
			
			return new LeaderboardEntry(incoming, HydrationState.HYDRATED);
		});
	}
	
	public void putPlayer(TieredPlayer player, HydrationState state) {
		putPlayer(player, state, null);
	}
	
	public void addHydratedPlayers(TieredPlayer[] players) {
		for (TieredPlayer player : players) {
			HydrationState state = getState(player);
			if (state.isHydrated()) {
				logger.warn(player.name() + " is already hydrated.");
				continue;
			}
			putPlayer(player, HydrationState.HYDRATED);
		}
	}
	
	public void addPartials(TieredPlayer[] players, Gamemode gamemode) {
		for (TieredPlayer player : players) {
			HydrationState state = getState(player);
			if (state.isHydrated()) {
				logger.warn(player.name() + " is already hydrated. Ignoring partial update.");
				continue;
			}
			putPlayer(player, HydrationState.PARTIAL, gamemode);
		}
	}
	
	public void reset() {
		this.players.clear();
	}
	
	public enum HydrationState {
		NONE,
		PARTIAL,
		HYDRATED;
		
		public boolean isNone() {
			return this == NONE;
		}
		
		public boolean isPartial() {
			return this == PARTIAL;
		}
		
		public boolean isHydrated() {
			return this == HYDRATED;
		}
	}
	
	public record LeaderboardEntry(TieredPlayer player, HydrationState state) {}
	
	public int getForegroundColour(int ranking) {
		int colour = 0xFFFFFFFF;
		
		if (ranking == 1) colour = 0xFFffd700;
		else if (ranking == 2) colour = 0xFFd6d6d6;
		else if (ranking == 3) colour = 0xFFce8946;
		else if (ranking >= 4 && ranking <= 15) colour = TierNametags.createGradient(0xFFec7dff, 0xFFa059c9, 12)[ranking - 4];
		else if (ranking >= 16 && ranking <= 30) colour = TierNametags.createGradient(0xFFa059c9, 0xFF596ac9, 15)[ranking - 16];
		else if (ranking >= 31 && ranking <= 50) colour = TierNametags.createGradient(0xFF596ac9, 0xFF5dbf58, 20)[ranking - 31];
		else if (ranking >= 51 && ranking <= 100) colour = 0xFF5dbf58;
		
		return colour;
	}
	
	public int getBackgroundColour(int ranking) {
		int colour = 0xAA000000;
		
		if (ranking == 1) colour = 0xc8ffd700;
		else if (ranking == 2) colour = 0xc8d6d6d6;
		else if (ranking == 3) colour = 0xc8ce8946;
		else if (ranking >= 4 && ranking <= 15) colour = TierNametags.createGradient(0xaaec7dff, 0xaaa059c9, 12)[ranking - 4];
		else if (ranking >= 16 && ranking <= 30) colour = TierNametags.createGradient(0xaaa059c9, 0xaa596ac9, 15)[ranking - 16];
		else if (ranking >= 31 && ranking <= 50) colour = TierNametags.createGradient(0xaa596ac9, 0xaa5dbf58, 20)[ranking - 31];
		else if (ranking >= 51 && ranking <= 100) colour = TierNametags.createGradient(0xaa5dbf58, 0xAA000000, 50)[ranking - 51];
		
		return colour;
	}
	
	public int getGradientStart(int ranking) {
		return switch (ranking) {
			case 1 -> 0xFFffe975;
			case 2 -> 0xFFe8e8e8;
			case 3 -> 0xFFf5b06e;
			default -> getBackgroundColour(ranking);
		};
	}
	
	public int getGradientEnd(int ranking) {
		return switch (ranking) {
			case 1 -> 0xFFdbc137;
			case 2 -> 0xFF9c9c9c;
			case 3 -> 0xFFbf7936;
			default -> getBackgroundColour(ranking);
		};
	}
	
	public int getTextColour(int ranking) {
		if (ranking <= 3) {
			return switch (ranking) {
				case 1 -> 0xFF8f7a10;
				case 2 -> 0xFF474747;
				case 3 -> 0xFF633c16;
				default -> getForegroundColour(ranking);
			};			
		} else {
			return (ranking <= 50) ? new Color(getForegroundColour(ranking)).darker().darker().darker().getRGB() | (0xFF << 24) : 0xFFFFFFFF;
		}
	}
}
