package me.mxngo.tiers;

public record TierTest(
	String player,
	String tester,
	Gamemode gamemode,
	Tier result,
	Tier prev,
	int timestamp
) {}