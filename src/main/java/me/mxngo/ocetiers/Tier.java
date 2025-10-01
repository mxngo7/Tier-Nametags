package me.mxngo.ocetiers;

public enum Tier {
	NONE((byte) (0b1 << 5), 0xFFB8B8B8, 0xFFB8B8B8, "textures/font/tiers/unranked.png"),
	HT((byte) (0b1 << 4), 0xFFFFFFFF, 0xFFFFFFFF),
	LT((byte) (0b1 << 3), 0xFFFFFFFF, 0xFFFFFFFF),
	
	HT1((byte) (HT.value | 0b001), 0xFF95EEF5, 0xFF7BDBE3, "textures/font/tiers/ht1.png"),
	HT2((byte) (HT.value | 0b010), 0xFF87E096, 0xFF6BDB7E, "textures/font/tiers/ht2.png"),
	HT3((byte) (HT.value | 0b011), 0xFFFDF55F, 0xFFEDE553, "textures/font/tiers/ht3.png"),
	HT4((byte) (HT.value | 0b100), 0xFFE0E0E0, 0xFFD4D4D4, "textures/font/tiers/ht4.png"),
	HT5((byte) (HT.value | 0b101), 0xFFFCA982, 0xFFFC9768, "textures/font/tiers/ht5.png"),
	
	LT1((byte) (LT.value | 0b001), 0xFF95EEF5, 0xFF7BDBE3, "textures/font/tiers/lt1.png"),
	LT2((byte) (LT.value | 0b010), 0xFF87E096, 0xFF6BDB7E, "textures/font/tiers/lt2.png"),
	LT3((byte) (LT.value | 0b011), 0xFFFDF55F, 0xFFEDE553, "textures/font/tiers/lt3.png"),
	LT4((byte) (LT.value | 0b100), 0xFFE0E0E0, 0xFFD4D4D4, "textures/font/tiers/lt4.png"),
	LT5((byte) (LT.value | 0b101), 0xFFFCA982, 0xFFFC9768, "textures/font/tiers/lt5.png");
	
	private final byte value;
	private final int lightColour;
	private final int darkColour;
	
	private final String icon;
	private final String iconPath;
	
	private Tier(byte value, int lightColour, int darkColour) {
		this.value = value;
		this.lightColour = lightColour;
		this.darkColour = darkColour;
		this.icon = new String(Character.toChars(0xE00A + this.ordinal() + 1));
		this.iconPath = null;
	}
	
	private Tier(byte value, int lightColour, int darkColour, String iconPath) {
		this.value = value;
		this.lightColour = lightColour;
		this.darkColour = darkColour;
		this.icon = new String(Character.toChars(0xE00A + this.ordinal() + 1));
		this.iconPath = iconPath;
	}
	
	private Tier(byte value, int lightColour, int darkColour, String icon, String iconPath) {
		this.value = value;
		this.lightColour = lightColour;
		this.darkColour = darkColour;
		this.icon = icon;
		this.iconPath = iconPath;
	}
	
	public byte getValue() {
		return this.value;
	}
	
	public int getLightColour() {
		return this.lightColour;
	}
	
	public int getDarkColour() {
		return this.darkColour;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public String getIconPath() {
		return this.iconPath;
	}
	
	public boolean isHighTier() {
		return ((this.value & Tier.HT.value) > 0);
	}
	
	public boolean isLowTier() {
		return ((this.value & Tier.LT.value) > 0);
	}
	
	public byte getTierValue() {
		if (this == Tier.NONE) return 0;
		return (byte) ((byte) ((5 - (this.getValue() - (this.isHighTier() ? Tier.HT.getValue() : Tier.LT.getValue()))) * 2 + 1) - (this.isLowTier() ? 1 : 0));
	}
	
	public boolean isHigherThan(Tier other) {
		return this.getTierValue() > other.getTierValue();
	}
	
	public boolean isLowerThan(Tier other) {
		return this.getTierValue() < other.getTierValue();
	}
	
	public boolean isEqual(Tier other) {
		return this.getTierValue() == other.getTierValue();
	}
	
	public long applyTo(Gamemode gamemode, long data) {
		int offset = gamemode.getOffset();
		return (data & ~(0xFFL << offset)) | ((long) this.value << offset);
	}
	
	public static Tier fromValue(byte value) {
		for (Tier tier : Tier.values()) {
			if (tier.value == value) return tier;
		}
		
		return Tier.NONE;
	}
	
	public static Tier fromName(String name) {
		for (Tier tier : Tier.values()) {
			if (tier.name().equalsIgnoreCase(name)) return tier;
		}
		
		return Tier.NONE;
	}
	
	public static long none() {
		long tierData = 0b0L;
		
		for (Gamemode gamemode : Gamemode.values()) {
			tierData = Tier.NONE.applyTo(gamemode, tierData);
		}
		
		return tierData;
	}
}
