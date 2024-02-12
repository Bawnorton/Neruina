package com.bawnorton.neruina.platform;

import java.nio.file.Path;

/*? if fabric {*//*
import net.fabricmc.loader.api.FabricLoader;

public final class Platform {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static Path getLogPath() {
        return FabricLoader.getInstance().getGameDir().resolve("logs/latest.log").toAbsolutePath();
    }

    public static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static ModLoader getModLoader() {
        return ModLoader.FABRIC;
    }
}
*//*? } elif forge {*/
import java.util.List;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public final class Platform {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static boolean isModLoaded(String modid) {
        List<ModInfo> mods = LoadingModList.get().getMods();
        for (ModInfo mod : mods) {
            if (mod.getModId().equals(modid)) {
                return true;
            }
        }
        return false;
    }

    public static Path getLogPath() {
        return FMLPaths.GAMEDIR.get().resolve("logs/latest.log").toAbsolutePath();
    }

    public static boolean isDev() {
        return !FMLLoader.isProduction();
    }

    public static ModLoader getModLoader() {
        return ModLoader.FORGE;
    }
}
/*? } elif neoforge {*//*
import java.util.List;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

public final class Platform {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static boolean isModLoaded(String modid) {
        List<ModInfo> mods = LoadingModList.get().getMods();
        for (ModInfo mod : mods) {
            if (mod.getModId().equals(modid)) {
                return true;
            }
        }
        return false;
    }

    public static Path getLogPath() {
        return FMLPaths.GAMEDIR.get().resolve("logs/latest.log").toAbsolutePath();
    }

    public static boolean isDev() {
        return !FMLLoader.isProduction();
    }

    public static ModLoader getModLoader() {
        return ModLoader.NEOFORGE;
    }
}
*//*? }*/
