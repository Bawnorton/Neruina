package com.bawnorton.neruina.platform;

import java.nio.file.Path;

/*? if fabric {*/
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.Config;

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
}
/*? } elif forge {*//*
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

    public static boolean isDev() {
        return !FMLLoader.isProduction();
    }

    public static ModLoader getModLoader() {
        return ModLoader.FORGE;
    }

    public static String modidFromJar(String jarName) {
        for (ModInfo mod : LoadingModList.get().getMods()) {
            if (mod.getOwningFile().getFile().getFilePath().endsWith(jarName)) {
                return mod.getModId();
            }
        }
        return null;
    }
}
*//*? } elif neoforge {*//*
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
}
*//*? }*/
