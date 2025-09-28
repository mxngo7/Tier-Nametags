package me.mxngo.config;

import me.mxngo.ocetiers.Gamemode;

public class TierNametagsConfig {
	public TierPosition tierPosition = TierPosition.LEFT;
	public Gamemode tierGamemode = Gamemode.SWORD;
	public boolean showOnNametags = true;
	public boolean showOnTablist = false;
	public boolean showTierText = false;
	public boolean reduceMotion = false;
	
	public TierNametagsConfig copy() {
		TierNametagsConfig copied = new TierNametagsConfig();
		copied.tierPosition = tierPosition;
		copied.tierGamemode = tierGamemode;
		copied.showOnNametags = showOnNametags;
		copied.showOnTablist = showOnTablist;
		copied.showTierText = showTierText;
		copied.reduceMotion = reduceMotion;
		return copied;
	}
}
