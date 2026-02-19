package me.mxngo.config;

import net.minecraft.text.Text;

public enum Tierlist {
	OCETIERS("OceTiers"),
	MCTIERS("MCTiers");
	
	private final String name;
	
	private Tierlist(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Text getIcon() {
		return Text.empty();
	}
	
	public boolean isOceTiers() {
		return this == OCETIERS;
	}
	
	public boolean isMCTiers() {
		return this == MCTIERS;
	}
}
