package com.bawnorton.neruina;

import com.bawnorton.neruina.networking.client.Networking;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeruinaClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(Neruina.MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.debug("Neruina Client Initialized");
		Networking.init();
	}
}