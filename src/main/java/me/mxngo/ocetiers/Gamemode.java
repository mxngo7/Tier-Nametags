package me.mxngo.ocetiers;

import me.mxngo.TierNametags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public enum Gamemode {
	SWORD("Sword", "textures/font/gamemodes/sword.png"),
	DIAMOND_POT("Diamond Pot", "textures/font/gamemodes/diamond_pot.png"),
	NETHERITE_POT("Netherite Pot", "textures/font/gamemodes/netherite_pot.png"),
	AXE("Axe", "textures/font/gamemodes/axe.png"),
	UHC("UHC", "textures/font/gamemodes/uhc.png"),
	SMP("SMP", "textures/font/gamemodes/smp.png"),
	CRYSTAL("Crystal", "textures/font/gamemodes/crystal.png"),
	DIAMOND_SMP("Diamond SMP", "textures/font/gamemodes/diamond_smp.png"),
	CART("Cart", "textures/font/gamemodes/cart.png", 0),
	MACE("Mace", "textures/font/gamemodes/mace.png", 8);
	
	private final String name;
	private final String commandString;
	private final int offset;
	private final String icon;
	private final String iconPath;
	
	private Gamemode(String name, String iconPath) {
		this.name = name;
		this.commandString = name.toLowerCase().replaceAll(" ", "_");
		this.offset = this.ordinal() * 8;
		this.icon = new String(Character.toChars(0xE000 + this.ordinal() + 1));
		this.iconPath = iconPath;
	}
	
	private Gamemode(String name, String iconPath, int offset) {
		this.name = name;
		this.commandString = name.toLowerCase().replaceAll(" ", "_");
		this.offset = offset;
		this.icon = new String(Character.toChars(0xE000 + this.ordinal() + 1));
		this.iconPath = iconPath;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getOffset() {
		return this.offset;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public String getIconPath() {
		return this.iconPath;
	}
	
	public Text getStyledIcon() {
		return Text.literal(getIcon()).styled(style -> style.withFont(Identifier.of(TierNametags.MODID, "icons")));
	}
	
	public String getCommandString() {
		return this.commandString;
	}
	
	public static Gamemode fromCommandString(String commandString) {
		for (Gamemode gamemode : Gamemode.values()) {
			if (gamemode.getCommandString().equalsIgnoreCase(commandString)) return gamemode;
		}
		
		return null;
	}
}
