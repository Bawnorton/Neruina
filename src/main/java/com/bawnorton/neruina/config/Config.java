package com.bawnorton.neruina.config;

public final class Config {
    private static Config INSTANCE;

    public LogLevel logLevel;

    public Boolean autoKillTickingEntities;

    public Integer tickingExceptionThreshold;

    public Boolean handleTickingEntities;

    public Boolean handleTickingBlockEntities;

    public Boolean handleTickingBlockStates;

    public Boolean handleTickingItemStacks;

    public Boolean handleTickingPlayers;

    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
        }
        return INSTANCE;
    }

    public static void update(Config config) {
        INSTANCE = config;
    }

    public enum LogLevel {
        EVERYONE, OPERATORS, DISABLED
    }
}
