package me.mxngo.ocetiers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import me.mxngo.TierNametags;

public class Leaderboard {
	private ArrayList<TieredPlayer> players = new ArrayList<>();
	private HashMap<Gamemode, List<TieredPlayer>> gamemodeToPlayersMap = new HashMap<>();
	
	public Leaderboard(TieredPlayer[] players) {
		this.players = new ArrayList<TieredPlayer>(List.of(players));
		this.players.sort(Comparator.comparingInt(TieredPlayer::getLeaderboardPoints).reversed());
		
		for (Gamemode gamemode : Gamemode.values()) {
			ArrayList<TieredPlayer> playersInGamemode = new ArrayList<>();
			
			for (TieredPlayer player : this.players) {
				if (player.getTier(gamemode) != Tier.NONE) playersInGamemode.add(player);
			}
			
			playersInGamemode.sort((a, b) -> Integer.compare(b.getTier(gamemode).getTierValue(), a.getTier(gamemode).getTierValue()));
			this.gamemodeToPlayersMap.put(gamemode, playersInGamemode);
		}
	}
	
	public List<TieredPlayer> getPlayers() {
		return this.players;
	}
	
	public List<TieredPlayer> getPlayers(Gamemode gamemode) {
		return this.gamemodeToPlayersMap.get(gamemode);
	}
	
	public List<TieredPlayer> getTop(int stop) {
		if (stop > this.players.size()) stop = this.players.size();
		return this.players.subList(0, stop);
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
