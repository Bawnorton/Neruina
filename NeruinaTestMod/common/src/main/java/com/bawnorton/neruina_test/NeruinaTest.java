package com.bawnorton.neruina_test;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeruinaTest {
    public static final String MOD_ID = "neruina_test";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Initializing NeruinaTest");
        NeruinaTest.LOGGER.debug("Registering Neruina Test Objects");
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
