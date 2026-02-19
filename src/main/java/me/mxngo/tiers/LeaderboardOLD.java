package me.mxngo.tiers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import me.mxngo.TierNametags;
import net.minecraft.util.Pair;

public class LeaderboardOLD {
	private ArrayList<TieredPlayer> players = new ArrayList<>();
	private final Map<String, Pair<CopyOnWriteArrayList<Gamemode>, TieredPlayer>> partiallyResolvedPlayers = new ConcurrentHashMap<>();
	private HashMap<Gamemode, List<TieredPlayer>> gamemodeToPlayersMap = new HashMap<>();
	private HashMap<Gamemode, Integer> gamemodeToPlayersFetched = new HashMap<>();
	private HashMap<Gamemode, Integer> gamemodeToPlayerCount = new HashMap<>();
	
	public LeaderboardOLD(TieredPlayer[] players) {
		this.setPlayers(players);
		for (Gamemode gamemode : Gamemode.values())
			gamemodeToPlayersFetched.put(gamemode, 0);
	}
	
	public List<TieredPlayer> getPlayers() {
		return this.players;
	}
	
	public List<TieredPlayer> getPlayers(Gamemode gamemode) {
		ArrayList<TieredPlayer> players = new ArrayList<>(this.gamemodeToPlayersMap.get(gamemode));
		List<TieredPlayer> partialPlayers = this.getPartiallyResolvedPlayers(gamemode);
		players.addAll(partialPlayers);
		return players;
	}
	
	public void setPlayers(TieredPlayer[] players) {
		this.players = new ArrayList<>(Arrays.asList(players));
		this.players.sort(Comparator.comparingInt(TieredPlayer::getLeaderboardPoints).reversed());
		
		for (TieredPlayer player : this.players) {
			if (this.isPartiallyResolved(player.name())) {
				partiallyResolvedPlayers.remove(player.name());
			}
		}
		
		for (Gamemode gamemode : Gamemode.values()) {
			ArrayList<TieredPlayer> playersInGamemode = new ArrayList<>();
			
			for (TieredPlayer player : this.players) {
				if (player.getTier(gamemode) != Tier.NONE && !player.getTier(gamemode).isRetired()) playersInGamemode.add(player);
			}
			
			playersInGamemode.sort((a, b) -> Integer.compare(b.getTier(gamemode).getTierValue(), a.getTier(gamemode).getTierValue()));
			this.gamemodeToPlayersMap.put(gamemode, playersInGamemode);
		}
	}
	
	public List<TieredPlayer> getTop(int stop) {
		if (stop > this.players.size()) stop = this.players.size();
		return this.players.subList(0, stop);
	}
	
	public int getNextOffset(Gamemode gamemode) {
		return gamemodeToPlayersFetched.get(gamemode);
	}
	
	public void updateNextOffset(Gamemode gamemode) {
		int offset = getNextOffset(gamemode);
		gamemodeToPlayersFetched.put(gamemode, offset + 50);
	}
	
	public void updatePlayerCount(Gamemode gamemode, int increase) {
		int previous = gamemodeToPlayerCount.getOrDefault(gamemode, 0);
		gamemodeToPlayerCount.put(gamemode, previous + increase);
	}
	
	public int getPlayerCount(Gamemode gamemode) {
		return gamemodeToPlayerCount.getOrDefault(gamemode, 0);
	}
	
	public boolean playerExists(String name) {
		return this.players.stream().filter(player ->
			player.name().equalsIgnoreCase(name)
		).findFirst().isPresent();
	}
	
	public void partiallyResolve(TieredPlayer player, Gamemode gamemode) {
		if (playerExists(player.name())) {
			partiallyResolvedPlayers.remove(player.name());
			return;
		}
		
		if (!partiallyResolvedPlayers.containsKey(player.name())) {
			TierNametags.getInstance().getLogger().info(player.name() + " contains no partial, creating one for " + gamemode.getName());
			partiallyResolvedPlayers.put(
				player.name(),
				new Pair<CopyOnWriteArrayList<Gamemode>, TieredPlayer>(
					new CopyOnWriteArrayList<Gamemode>(List.of(gamemode)), player
				)
			);
		} else {
			TierNametags.getInstance().getLogger().info(player.name() + " is already partialed, updating " + gamemode.getName());
			Pair<CopyOnWriteArrayList<Gamemode>, TieredPlayer> pair = partiallyResolvedPlayers.get(player.name());
			if (pair.getLeft().contains(gamemode)) {
				TierNametags.getInstance().getLogger().warn(player.name() + "'s partial already contains this gamemode");
				return;
			}
			pair.setRight(pair.getRight().with(gamemode, player.getTier(gamemode)));
			pair.getLeft().add(gamemode);
			partiallyResolvedPlayers.put(player.name(), pair);
		}
	}
	
	public List<TieredPlayer> getPartiallyResolvedPlayers(Gamemode gamemode) {
		List<Pair<CopyOnWriteArrayList<Gamemode>, TieredPlayer>> copy =
	        new ArrayList<>(partiallyResolvedPlayers.values());
		
		return copy.stream()
			.filter(pair -> this.isPartiallyResolved(pair.getRight().name(), gamemode))
			.map(pair -> pair.getRight())
			.toList();
	}
	
	public boolean isPartiallyResolved(String name) {
		return this.partiallyResolvedPlayers.containsKey(name);
	}
	
	public boolean isPartiallyResolved(String name, Gamemode gamemode) {
		if (!isPartiallyResolved(name)) return false;
		Pair<CopyOnWriteArrayList<Gamemode>, TieredPlayer> pair = partiallyResolvedPlayers.get(name);
		if (pair == null) return false;
		return pair.getLeft().contains(gamemode);
	}
	
	public void updatePartialPlayers(TieredPlayer[] players, Gamemode gamemode) {
		for (TieredPlayer player : players)
			partiallyResolve(player, gamemode);
	}
	
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
