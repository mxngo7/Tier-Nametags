package me.mxngo.config;

import me.mxngo.tiers.Gamemode;
import me.mxngo.update.VersionChecker;

public class TierNametagsConfig {
	public String version = VersionChecker.getCurrentVersion();
	
	public Gamemode gamemode = Gamemode.SWORD;
	public Tierlist tierlist = Tierlist.MCTIERS;
	
	public DisplayType nametags = new DisplayType(true, true, true, false, TierPosition.LEFT);
	public DisplayType playerList = new DisplayType(false, true, true, false, TierPosition.LEFT);
	public DisplayType chat = new DisplayType(false, true, true, false, TierPosition.LEFT);
	
	public boolean reduceMotion = false;
	
	public TierNametagsConfig copy() {
		TierNametagsConfig copied = new TierNametagsConfig();
		copied.gamemode = gamemode;
		copied.tierlist = tierlist;
		copied.nametags = nametags;
		copied.playerList = playerList;
		copied.chat = chat;
		copied.reduceMotion = reduceMotion;
		return copied;
	}
}
