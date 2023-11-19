package com.bawnorton.neruina.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class PlatformImpl {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static String getMinecraftVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion().getFriendlyString();
    }
}
