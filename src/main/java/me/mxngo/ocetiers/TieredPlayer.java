package me.mxngo.ocetiers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

import me.mxngo.ui.ProfileTheme;

public record TieredPlayer(@SerializedName("name") String name, @SerializedName("data") long[] data) {
	private static final LocalDate epoch = LocalDate.of(2000, 1, 1);
	
	public Tier getTier(Gamemode gamemode) {
		return switch (gamemode) {
			case Gamemode.CART, Gamemode.MACE -> Tier.fromValue((byte) ((this.data[1] >> gamemode.getOffset()) & 0xFFL));
			default -> Tier.fromValue((byte) ((this.data[0] >> gamemode.getOffset()) & 0xFFL));
		};
	}
	
	public TieredPlayer with(Gamemode gamemode, Tier tier) {
		return switch (gamemode) {
			case Gamemode.CART, Gamemode.MACE -> new TieredPlayer(this.name, new long[] {this.data[0], tier.applyTo(gamemode, this.data[1])});
			default -> new TieredPlayer(this.name, new long[] {tier.applyTo(gamemode, this.data[0]), this.data[1]});
		};
	}
	
	public int getLeaderboardPosition() {
		return (int) (this.data[1] >> 16) & 0xFFFF;
	}
	
	public int getLeaderboardPoints() {
		return (int) (this.data[1] >> 32) & 0xFFFF;
	}
	
	public long getDaysSinceLastUpdate() {
		return ChronoUnit.DAYS.between(epoch.plusDays(((this.data[1]) >> 48) & 0xFFFF), LocalDate.now());
	}
	
	public Tier getBestTier() {
		Optional<Tier> tier = List.of(Gamemode.values()).stream().map(this::getTier).sorted(Comparator.comparingInt(Tier::getTierValue).reversed()).findFirst();
		return tier.isPresent() ? tier.get() : Tier.NONE;
	}
	
	public ProfileTheme getProfileTheme() {
		int position = getLeaderboardPosition();
		
		if (position <= 3) {
			return switch(getLeaderboardPosition()) {
				case 1 -> ProfileTheme.YELLOW;
				case 2 -> ProfileTheme.GREY;
				case 3 -> ProfileTheme.BRONZE;
				default -> ProfileTheme.DEFAULT;
			};
		}
		
		else if (position <= 25) return ProfileTheme.PINK;
		else if (position <= 40) return ProfileTheme.BLUE;
		else if (position <= 75) return ProfileTheme.GREEN;
		else return ProfileTheme.DEFAULT;
	}
}