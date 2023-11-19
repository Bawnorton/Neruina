package com.bawnorton.neruina.config;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.Platform;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    private static final Path configPath = Platform.getConfigDir().resolve(Neruina.MOD_ID + ".json");

    public static void loadConfig() {
        Config config = load();

        if (config.broadcastErrors == null) config.broadcastErrors = true;
        if (config.handleTickingEntities == null) config.handleTickingEntities = true;
        if (config.handleTickingBlockEntities == null) config.handleTickingBlockEntities = true;
        if (config.handleTickingBlockStates == null) config.handleTickingBlockStates = true;
        if (config.handleTickingItemStacks == null) config.handleTickingItemStacks = true;
        if (config.handleTickingPlayers == null) config.handleTickingPlayers = true;

        Config.update(config);
        saveConfig();
    }

    private static Config load() {
        Config config = Config.getInstance();
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
                return config;
            }
            try {
                config = GSON.fromJson(Files.newBufferedReader(configPath), Config.class);
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
            Files.write(configPath, GSON.toJson(Config.getInstance()).getBytes());
        } catch (IOException e) {
            Neruina.LOGGER.error("Failed to save config", e);
        }
    }

    public static void saveConfig() {
        save();
        Neruina.LOGGER.debug("Saved client config");
    }
}
