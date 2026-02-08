//? if <=1.20.1 {
/*package com.bawnorton.neruina.config;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.platform.Platform;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
	public static final Path CONFIG_PATH = Platform.getConfigDir().resolve(Neruina.MOD_ID + ".json");
	private static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.setPrettyPrinting()
			.create();

	public static void load() {
		Config.DummyInstance instance = new Config.DummyInstance();
		try {
			if (!Files.exists(CONFIG_PATH)) {
				Files.createDirectories(CONFIG_PATH.getParent());
				Files.createFile(CONFIG_PATH);
				save();
				return;
			}
			try {
				instance = GSON.fromJson(Files.newBufferedReader(CONFIG_PATH), Config.DummyInstance.class);
			} catch (JsonSyntaxException e) {
				Neruina.LOGGER.error("Failed to load config, using defaults", e);
			}
		} catch (IOException e) {
			Neruina.LOGGER.error("Failed to load config, using defaults", e);
		}

		if (instance == null) {
			Neruina.LOGGER.warn("Config file was empty, using defaults");
			instance = new Config.DummyInstance();
		}

		if (instance.minPermissionLevelForMessages == null) {
			instance.minPermissionLevelForMessages = 0;
		}
		if (instance.minPermissionLevelForCommands == null) {
			instance.minPermissionLevelForCommands = 2;
		}
		if (instance.autoKillTickingEntities == null) {
			instance.autoKillTickingEntities = false;
		}
		if (instance.tickingExceptionThreshold == null) {
			instance.tickingExceptionThreshold = 10;
		}
		if (instance.handleTickingEntities == null) {
			instance.handleTickingEntities = true;
		}
		if (instance.handleTickingBlockEntities == null) {
			instance.handleTickingBlockEntities = true;
		}
		if (instance.handleTickingBlockStates == null) {
			instance.handleTickingBlockStates = true;
		}
		if (instance.handleTickingItemStacks == null) {
			instance.handleTickingItemStacks = true;
		}
		if (instance.handleTickingPlayers == null) {
			instance.handleTickingPlayers = true;
		}

		Config.minPermissionLevelForMessages = instance.minPermissionLevelForMessages;
		Config.minPermissionLevelForCommands = instance.minPermissionLevelForCommands;
		Config.autoKillTickingEntities = instance.autoKillTickingEntities;
		Config.tickingExceptionThreshold = instance.tickingExceptionThreshold;
		Config.handleTickingEntities = instance.handleTickingEntities;
		Config.handleTickingBlockEntities = instance.handleTickingBlockEntities;
		Config.handleTickingBlockStates = instance.handleTickingBlockStates;
		Config.handleTickingItemStacks = instance.handleTickingItemStacks;
		Config.handleTickingPlayers = instance.handleTickingPlayers;

		save();
	}

	public static void save() {
		Config.DummyInstance instance = new Config.DummyInstance();
		instance.minPermissionLevelForMessages = Config.minPermissionLevelForMessages;
		instance.minPermissionLevelForCommands = Config.minPermissionLevelForCommands;
		instance.autoKillTickingEntities = Config.autoKillTickingEntities;
		instance.tickingExceptionThreshold = Config.tickingExceptionThreshold;
		instance.handleTickingEntities = Config.handleTickingEntities;
		instance.handleTickingBlockEntities = Config.handleTickingBlockEntities;
		instance.handleTickingBlockStates = Config.handleTickingBlockStates;
		instance.handleTickingItemStacks = Config.handleTickingItemStacks;
		instance.handleTickingPlayers = Config.handleTickingPlayers;

		try {
			if (!Files.exists(CONFIG_PATH)) {
				Files.createDirectories(CONFIG_PATH.getParent());
				Files.createFile(CONFIG_PATH);
			}
			Files.write(CONFIG_PATH, GSON.toJson(instance).getBytes());
		} catch (IOException e) {
			Neruina.LOGGER.error("Failed to save config", e);
		}
	}
}
*///?}