package com.bawnorton.neruina.platform;


//? if fabric {

import java.nio.file.Path;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;

public final class Platform {
	public static Path getConfigDir() {
    return FabricLoader.getInstance().getConfigDir();
  }

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public static ModLoader getModLoader() {
		return ModLoader.FABRIC;
	}

	public static String modidFromJar(String jarName) {
		for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
			ModMetadata metadata = modContainer.getMetadata();
			ModOrigin origin = modContainer.getOrigin();
			switch (origin.getKind()) {
				case PATH -> {
					for (Path path : origin.getPaths()) {
						if (path.endsWith(jarName)) {
							return metadata.getId();
						}
					}
				}
				case NESTED -> {
					String parentLocation = origin.getParentSubLocation();
					if (parentLocation != null && parentLocation.endsWith(jarName)) {
						return metadata.getId();
					}
				}
			}
		}
		return null;
	}

	public static String getModVersion(String modid) {
		return FabricLoader.getInstance().getModContainer(modid).map(ModContainer::getMetadata).map(modMetadata -> modMetadata.getVersion().getFriendlyString()).orElse("unknown");
	}

	public static String getVersion() {
		return getModVersion("fabricloader");
	}

	public static boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT);
	}

	public static boolean isDev() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
}
//?} elif neoforge {
/*import java.util.List;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

public final class Platform {
	public static ModLoader getModLoader() {
		return ModLoader.NEOFORGE;
	}

	public static String getModVersion(String modid) {
		return ModList.get().getModFileById(modid).versionString();
	}

	//? if >=1.21.10 {
	/^public static boolean isModLoaded(String modid) {
		List<ModInfo> mods = FMLLoader.getCurrent().getLoadingModList().getMods();
		for (ModInfo mod : mods) {
			if (mod.getModId().equals(modid)) {
				return true;
			}
		}
		return false;
	}

	public static String modidFromJar(String jarName) {
		for (ModInfo mod : FMLLoader.getCurrent().getLoadingModList().getMods()) {
			String modLocation = mod.getOwningFile()
					.getFile()
					.getFilePath()
					.toString()
					.replace("+", " ");

			String decodedJarName = URLDecoder.decode(jarName, StandardCharsets.UTF_8);
			int hashIndex = decodedJarName.lastIndexOf("#");
			if (hashIndex != -1) {
				decodedJarName = decodedJarName.substring(0, hashIndex);
			}
			if (modLocation.endsWith(decodedJarName)) {
				return mod.getModId();
			}
		}
		return null;
	}

	public static String getVersion() {
		return FMLLoader.getCurrent().getVersionInfo().neoForgeVersion();
	}

	public static boolean isClient() {
		return FMLLoader.getCurrent().getDist().isClient();
	}

	public static boolean isDev() {
		return !FMLLoader.getCurrent().isProduction();
	}
	^///?} else {
	public static boolean isModLoaded(String modid) {
		List<ModInfo> mods = LoadingModList.get().getMods();
		for (ModInfo mod : mods) {
			if (mod.getModId().equals(modid)) {
				return true;
			}
		}
		return false;
	}

	public static String modidFromJar(String jarName) {
		for (ModInfo mod : LoadingModList.get().getMods()) {
			String modLocation = mod.getOwningFile()
					.getFile()
					.getFilePath()
					.toString()
					.replace("+", " ");

			String decodedJarName = URLDecoder.decode(jarName, StandardCharsets.UTF_8);
			int hashIndex = decodedJarName.lastIndexOf("#");
			if (hashIndex != -1) {
				decodedJarName = decodedJarName.substring(0, hashIndex);
			}
			if (modLocation.endsWith(decodedJarName)) {
				return mod.getModId();
			}
		}
		return null;
	}

	public static String getVersion() {
		return FMLLoader.versionInfo().neoForgeVersion();
	}

	public static boolean isClient() {
		return FMLLoader.getDist().isClient();
	}

	public static boolean isDev() {
		return !FMLLoader.isProduction();
	}
	//?}
}
*///?} elif forge {

/*import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.nio.file.Path;
import java.util.List;

public final class Platform {
	public static boolean isModLoaded(String modid) {
		List<ModInfo> mods = LoadingModList.get().getMods();
		for (ModInfo mod : mods) {
			if (mod.getModId().equals(modid)) {
				return true;
			}
		}
		return false;
	}

	public static ModLoader getModLoader() {
		return ModLoader.FORGE;
	}

	public static String modidFromJar(String jarName) {
		for (ModInfo mod : LoadingModList.get().getMods()) {
			String modLocation = mod.getOwningFile()
					.getFile()
					.getFilePath()
					.toString()
					.replace("+", " ");

			if (modLocation.endsWith(jarName)) {
				return mod.getModId();
			}
		}
		return null;
	}

	public static String getVersion() {
		return FMLLoader.versionInfo().forgeVersion();
	}

	public static boolean isClient() {
		return FMLLoader.getDist().isClient();
	}

	public static boolean isDev() {
		return !FMLLoader.isProduction();
	}

	public static String getModVersion(String modid) {
		return LoadingModList.get().getModFileById(modid).versionString();
	}

	public static Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}
}
*///?}
