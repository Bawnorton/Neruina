package com.bawnorton.neruina.forge;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.nio.file.Path;
import java.util.List;

public class PlatformImpl {
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

    public static String getMinecraftVersion() {
        return LoadingModList.get().getModFileById("minecraft").versionString();
    }
}
