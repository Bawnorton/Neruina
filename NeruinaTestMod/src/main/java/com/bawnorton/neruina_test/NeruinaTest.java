package com.bawnorton.neruina_test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeruinaTest implements ModInitializer {
	public static final String MOD_ID = "neruina_test";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Neruina Test Mod");
		NeruinaTestRegistrar.init();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}