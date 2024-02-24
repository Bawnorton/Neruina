package com.bawnorton.neruina.platform;

import java.nio.file.Path;

/*? if fabric {*//*
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
public final class Platform {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static ModLoader getModLoader() {
        return ModLoader.FABRIC;
    }

    public static String modidFromJar(String jarName) {
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            for (Path path : modContainer.getOrigin().getPaths()) {
                if (path.endsWith(jarName)) {
                    return modContainer.getMetadata().getId();
                }
            }
        }
        return null;
    }

    public static String getModVersion(String modid) {
        return FabricLoader.getInstance().getModContainer(modid).map(ModContainer::getMetadata).map(modMetadata -> modMetadata.getVersion().getFriendlyString()).orElse("unknown");
    }
}
*//*? } elif forge {*/
import java.util.List;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;

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

    public static boolean isDev() {
        return !FMLLoader.isProduction();
    }

    public static ModLoader getModLoader() {
        return ModLoader.FORGE;
    }

    public static String modidFromJar(String jarName) {
        for (IModInfo mod : ModList.get().getMods()) {
            if (mod.getOwningFile().getFile().getFilePath().endsWith(jarName)) {
                return mod.getModId();
            }
        }
        return null;
    }

    public static String getModVersion(String modid) {
        return ModList.get().getModFileById(modid).versionString();
    }
}
/*? } elif neoforge {*//*
import java.util.List;
import net.neoforged.fml.ModList;
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

    public static boolean isDev() {
        return !FMLLoader.isProduction();
    }

    public static ModLoader getModLoader() {
        return ModLoader.NEOFORGE;
    }

    public static String modidFromJar(String jarName) {
        for (ModInfo mod : LoadingModList.get().getMods()) {
            if (mod.getOwningFile().getFile().getFilePath().endsWith(jarName)) {
                return mod.getModId();
            }
        }
        return null;
    }

    public static String getModVersion(String modid) {
        return ModList.get().getModFileById(modid).versionString();
    }
}
*//*? }*/
