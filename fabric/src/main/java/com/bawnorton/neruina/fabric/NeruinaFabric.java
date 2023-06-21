package com.bawnorton.neruina.fabric;

import com.bawnorton.neruina.Neruina;
import net.fabricmc.api.ModInitializer;

public class NeruinaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Neruina.init();
    }
}
