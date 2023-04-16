package com.bawnorton.neruina;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeruinaClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("neruina");

	@Override
	public void onInitializeClient() {
		LOGGER.debug("Neruina has no client-side functionality.");
	}
}