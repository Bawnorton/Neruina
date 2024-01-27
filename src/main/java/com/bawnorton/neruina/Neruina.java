package com.bawnorton.neruina;

import com.bawnorton.neruina.config.ConfigManager;
import com.bawnorton.neruina.handler.NeruinaTickHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neruina {
    public static final String MOD_ID = "neruina";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final NeruinaTickHandler TICK_HANDLER = new NeruinaTickHandler();

    public static void init() {
        ConfigManager.loadConfig();
    }
}