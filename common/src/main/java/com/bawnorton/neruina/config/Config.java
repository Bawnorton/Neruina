package com.bawnorton.neruina.config;

import com.google.gson.annotations.Expose;

public class Config {
    private static Config INSTANCE;

    @Expose
    public Boolean broadcastErrors;

    @Expose
    public Boolean handleTickingEntities;

    @Expose
    public Boolean handleTickingBlockEntities;

    @Expose
    public Boolean handleTickingBlockStates;

    @Expose
    public Boolean handleTickingItemStacks;

    @Expose
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
}
