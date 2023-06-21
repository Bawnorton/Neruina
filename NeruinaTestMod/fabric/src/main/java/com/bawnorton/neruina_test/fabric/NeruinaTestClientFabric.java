package com.bawnorton.neruina_test.fabric;

import com.bawnorton.neruina_test.client.NeruinaTestClient;
import net.fabricmc.api.ClientModInitializer;

public class NeruinaTestClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NeruinaTestClient.init();
    }
}
