package me.mxngo.config;

import com.google.gson.annotations.SerializedName;

public record DisplayType(
	@SerializedName("enabled") boolean enabled,
	@SerializedName("tierIcon") boolean tierIcon,
	@SerializedName("gamemodeIcon") boolean gamemodeIcon,
	@SerializedName("tierText") boolean tierText,
	@SerializedName("position") TierPosition position
) {}