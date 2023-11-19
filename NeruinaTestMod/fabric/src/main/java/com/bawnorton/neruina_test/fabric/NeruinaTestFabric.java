package com.bawnorton.neruina_test.fabric;

import com.bawnorton.neruina_test.NeruinaTest;
import net.fabricmc.api.ModInitializer;

public class NeruinaTestFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NeruinaTest.init();
    }
}
