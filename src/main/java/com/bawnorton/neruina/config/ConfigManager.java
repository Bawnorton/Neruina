package com.bawnorton.neruina.config;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.platform.Platform;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    public static final Path CONFIG_PATH = Platform.getConfigDir().resolve(Neruina.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    private ConfigManager() {
    }

    public static void loadConfig() {
        Config config = load();

        if (config.logLevel == null) {
            config.logLevel = Config.LogLevel.OPERATORS;
        }
        if (config.autoKillTickingEntities == null) {
            config.autoKillTickingEntities = false;
        }
        if (config.tickingExceptionThreshold == null) {
            config.tickingExceptionThreshold = 10;
        }
        if (config.handleTickingEntities == null) {
            config.handleTickingEntities = true;
        }
        if (config.handleTickingBlockEntities == null) {
            config.handleTickingBlockEntities = true;
        }
        if (config.handleTickingBlockStates == null) {
            config.handleTickingBlockStates = true;
        }
        if (config.handleTickingItemStacks == null) {
            config.handleTickingItemStacks = true;
        }
        if (config.handleTickingPlayers == null) {
            config.handleTickingPlayers = true;
        }

        Config.update(config);
        saveConfig();
    }

    private static Config load() {
        Config config = Config.getInstance();
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.createFile(CONFIG_PATH);
                return config;
            }
            try {
                config = GSON.fromJson(Files.newBufferedReader(CONFIG_PATH), Config.class);
            } catch (JsonSyntaxException e) {
                Neruina.LOGGER.error("Failed to parse config file, using default config");
                config = new Config();
            }
        } catch (IOException e) {
            Neruina.LOGGER.error("Failed to load config", e);
        }
        return config;
    }

    private static void save() {
        try {
            Files.write(CONFIG_PATH, GSON.toJson(Config.getInstance()).getBytes());
        } catch (IOException e) {
            Neruina.LOGGER.error("Failed to save config", e);
        }
    }

    public static void saveConfig() {
        save();
    }
}
