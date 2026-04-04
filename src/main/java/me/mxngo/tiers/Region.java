package me.mxngo.tiers;

import java.util.Arrays;
import java.util.Optional;

public enum Region {
	NA("North America", "NA", 0xff57AEff, 0xff357EDE),
    EU("Europe", "EU", 0xff788aff, 0xff575fd9),
    SA("South America", "SA", 0xff22C55E, 0xff16A34A),
    AU("Australia", "AU", 0xffF59E0B, 0xffD97706),
    ME("Middle East", "ME", 0xffE11D48, 0xffBE123C),
    AS("Asia", "AS", 0xffA855F7, 0xff9333EA),
    AF("Africa", "AF", 0xffF97316, 0xffEA580C),
    UNKNOWN("Unknown", "??", 0xff6B7280, 0xff4B5563);
	
	private final String name, code;
	private final int lightColour, darkColour;
	
	private Region(String name, String code, int lightColour, int darkColour) {
		this.name = name;
		this.code = code;
		this.lightColour = lightColour;
		this.darkColour = darkColour;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public int getLightColour() {
		return this.lightColour;
	}
	
	public int getDarkColour() {
		return this.darkColour;
	}
	
	public boolean isUnknown() {
		return this == UNKNOWN;
	}
	
	public static Region fromCode(String code) {
		Optional<Region> region = Arrays.asList(Region.values()).stream()
			.filter(r -> r.getCode().equals(code))
			.findFirst();
		return region.isPresent() ? region.get() : UNKNOWN;
	}
}
