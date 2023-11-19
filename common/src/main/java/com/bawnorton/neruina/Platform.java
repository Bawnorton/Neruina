package com.bawnorton.neruina;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public abstract class Platform {
    @ExpectPlatform
    public static Path getConfigDir() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modid) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String getMinecraftVersion() {
        throw new AssertionError();
    }
}
