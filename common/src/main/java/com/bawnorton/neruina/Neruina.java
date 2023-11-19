package com.bawnorton.neruina;

import com.bawnorton.neruina.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neruina {
    public static final String MOD_ID = "neruina";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ConfigManager.loadConfig();
        LOGGER.info("Initializing Neruina");
    }
}
