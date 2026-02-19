package me.mxngo.tiers;

import me.mxngo.TierNametags;
import me.mxngo.config.Tierlist;
import me.mxngo.tiers.wrappers.MCTiersAPIWrapper;
import me.mxngo.tiers.wrappers.OceTiersAPIWrapper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class TierlistManager {
	private final TierNametags tierNametags = TierNametags.getInstance();
	
	private final OceTiersAPIWrapper oceTiersAPIWrapper = new OceTiersAPIWrapper();
	private final Leaderboard oceTiersLeaderboard = new Leaderboard("OceTiers");
	
	private final MCTiersAPIWrapper mcTiersAPIWrapper = new MCTiersAPIWrapper(this);
	private final Leaderboard mcTiersLeaderboard = new Leaderboard("MCTiers");
	
	public void doInitialFetch() {
		fetchOceTiersPlayers();
		fetchMCTiersPlayers();
	}
	
	private void fetchOceTiersPlayers(FabricClientCommandSource source) {
		if (source != null)
			source.sendFeedback(tierNametags.getTierNametagsChatLabel().append(" Fetching profiles from ").append(oceTiersAPIWrapper.getApiUrl()));
		
		oceTiersAPIWrapper.getPlayers().thenAccept(players -> {
			oceTiersLeaderboard.addHydratedPlayers(players);
			oceTiersLeaderboard.getLogger().info("Loaded " + players.length + " hydrated players.");
			if (source != null)
				source.sendFeedback(tierNametags.getTierNametagsChatLabel().append(" Loaded " + players.length + " hydrated players. [OceTiers]"));
		}).exceptionally(exception -> {
			oceTiersLeaderboard.getLogger().error("Failed to load players.");
			if (source != null)
				source.sendError(tierNametags.getTierNametagsChatLabel(0xFF5959, 0x9C0909).append(" Failed to fetch. Is ").append(oceTiersAPIWrapper.getApiUrl()).append(" offline?"));
			exception.printStackTrace();
			return null;
		});
	}
	
	private void fetchOceTiersPlayers() {
		fetchOceTiersPlayers(null);
	}
	
	private void fetchMCTiersPlayers(FabricClientCommandSource source) {
		if (source != null)
			source.sendFeedback(tierNametags.getTierNametagsChatLabel().append(" Fetching profiles from ").append(mcTiersAPIWrapper.getApiUrl()));
		
		fetchMCTiersGamemode(null, source);
		for (Gamemode gamemode : Gamemode.getMCTiersGamemodes()) {
			fetchMCTiersGamemode(gamemode, source);
		}
	}
	
	private void fetchMCTiersPlayers() {
		fetchMCTiersPlayers(null);
	}
	
	private void fetchMCTiersGamemode(Gamemode gamemode, FabricClientCommandSource source) {
		int from = mcTiersAPIWrapper.getOffset(gamemode);
		
		mcTiersAPIWrapper.getPlayers(from, gamemode).thenAccept(players -> {
			if (gamemode == null) {
				mcTiersLeaderboard.addHydratedPlayers(players);
				mcTiersLeaderboard.getLogger().info("Loaded " + players.length + " hydrated players.");
				if (source != null)
					source.sendFeedback(tierNametags.getTierNametagsChatLabel().append(" Loaded " + players.length + " hydrated players. [MCTiers]"));
			} else {
				mcTiersLeaderboard.addPartials(players, gamemode);
				mcTiersLeaderboard.getLogger().info("Loaded " + players.length + " partial players for " + gamemode.getName() + ". [MCTiers]");
				if (source != null)
					source.sendFeedback(tierNametags.getTierNametagsChatLabel().append(" Loaded " + players.length + " partial players for " + gamemode.getName() + "."));
			}
		}).exceptionally(exception -> {
			mcTiersLeaderboard.getLogger().error("Failed to load players.");
			if (source != null)
				source.sendError(tierNametags.getTierNametagsChatLabel(0xFF5959, 0x9C0909).append(" Failed to fetch. Is ").append(mcTiersAPIWrapper.getApiUrl()).append(" offline?"));
			exception.printStackTrace();
			return null;
		});
	}
	
	public APIWrapper getAPIWrapper(Tierlist tierlist) {
		return switch (tierlist) {
			case Tierlist.OCETIERS -> this.oceTiersAPIWrapper;
			case Tierlist.MCTIERS -> this.mcTiersAPIWrapper;
			default -> null;
		};
	}
	
	public APIWrapper getAPIWrapper() {
		return getAPIWrapper(getActiveTierlist());
	}
	
	public Tierlist getActiveTierlist() {
		return tierNametags.getConfig().tierlist;
	}
	
	public Leaderboard getLeaderboard(Tierlist tierlist) {
		return switch (getActiveTierlist()) {
			case Tierlist.OCETIERS -> this.oceTiersLeaderboard;
			case Tierlist.MCTIERS -> this.mcTiersLeaderboard;
			default -> null;
		};
	}
	
	public Leaderboard getActiveLeaderboard() {
		return getLeaderboard(getActiveTierlist());
	}
	
	public void refetchLeaderboard(Tierlist tierlist, FabricClientCommandSource source) {
		switch (tierlist) {
			case Tierlist.OCETIERS:
				oceTiersLeaderboard.reset();
				fetchOceTiersPlayers(source);
				break;
			case Tierlist.MCTIERS:
				mcTiersLeaderboard.reset();
				mcTiersAPIWrapper.resetOffsets();
				fetchMCTiersPlayers(source);
				break;
			default:
				break;
		};
	}
	
	public void refetchLeaderboard() {
		refetchLeaderboard(getActiveTierlist(), null);
	}
	
	public void refetchLeaderboard(FabricClientCommandSource source) {
		refetchLeaderboard(getActiveTierlist(), source);
	}
}
