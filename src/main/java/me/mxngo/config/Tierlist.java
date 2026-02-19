package me.mxngo.config;

import me.mxngo.TierNametags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public enum Tierlist {
	OCETIERS("OceTiers", "font/tiers/ocetiershd.png"),
	MCTIERS("MCTiers", "font/tiers/mctiershd.png");
	
	private final String name, iconPath;
	
	private Tierlist(String name, String iconPath) {
		this.name = name;
		this.iconPath = iconPath;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Text getIcon() {
		return switch (this) {
			case OCETIERS -> getStyledIcon("\uE01D");
			case MCTIERS -> getStyledIcon("\uE01E");
			default -> Text.empty();
		};
	}
	
	public Identifier getIconPath() {
		return Identifier.of(TierNametags.MODID, this.iconPath);
	}
	
	private Text getStyledIcon(String icon) {
		return Text.literal(icon).styled(style -> style.withFont(Identifier.of(TierNametags.MODID, "icons")));
	}
	
	public boolean isOceTiers() {
		return this == OCETIERS;
	}
	
	public boolean isMCTiers() {
		return this == MCTIERS;
	}
}
