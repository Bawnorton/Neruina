package com.bawnorton.neruina.config;

import com.bawnorton.configurable.Configurable;

public final class Config {
	@Configurable
	public static Integer minPermissionLevelForMessages = 0;

	@Configurable
	public static Integer minPermissionLevelForCommands = 2;

	@Configurable
	public static Boolean autoKillTickingEntities = false;

	@Configurable
	public static Integer tickingExceptionThreshold = 10;

	@Configurable
	public static Boolean handleTickingEntities = true;

	@Configurable
	public static Boolean handleTickingBlockEntities = true;

	@Configurable
	public static Boolean handleTickingBlockStates = true;

	@Configurable
	public static Boolean handleTickingItemStacks = true;

	@Configurable
	public static Boolean handleTickingPlayers = true;
}
