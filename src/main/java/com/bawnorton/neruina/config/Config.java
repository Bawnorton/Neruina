package com.bawnorton.neruina.config;

//? if >1.20.1 {
import com.bawnorton.configurable.Configurable;
//?}

public final class Config {
	//? if >1.20.1 {
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
	//?} else {
	/*public static Integer minPermissionLevelForMessages = 0;

	public static Integer minPermissionLevelForCommands = 2;

	public static Boolean autoKillTickingEntities = false;

	public static Integer tickingExceptionThreshold = 10;

	public static Boolean handleTickingEntities = true;

	public static Boolean handleTickingBlockEntities = true;

	public static Boolean handleTickingBlockStates = true;

	public static Boolean handleTickingItemStacks = true;

	public static Boolean handleTickingPlayers = true;

	static class DummyInstance {
		Integer minPermissionLevelForMessages = 0;

		Integer minPermissionLevelForCommands = 2;

		Boolean autoKillTickingEntities = false;

		Integer tickingExceptionThreshold = 10;

		Boolean handleTickingEntities = true;

		Boolean handleTickingBlockEntities = true;

		Boolean handleTickingBlockStates = true;

		Boolean handleTickingItemStacks = true;

		Boolean handleTickingPlayers = true;
	}
	*///?}
}
