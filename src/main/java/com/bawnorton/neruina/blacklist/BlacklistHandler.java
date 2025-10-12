package com.bawnorton.neruina.blacklist;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.util.ErroredType;
import com.google.gson.stream.JsonReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class BlacklistHandler {
	private final Map<ResourceLocation, Blacklist> blacklists = new HashMap<>();

	public void init(MinecraftServer server) {
		Map<ResourceLocation, Resource> blacklistFiles = server.getResourceManager().listResources(Neruina.MOD_ID, (resource) -> resource.getPath().equals("neruina/blacklist.json"));
		if (blacklistFiles.isEmpty()) {
			Neruina.LOGGER.info("No blacklist files found, skipping blacklist loading");
			return;
		}
		for (Map.Entry<ResourceLocation, Resource> entry : blacklistFiles.entrySet()) {
			ResourceLocation id = entry.getKey();
			Resource resource = entry.getValue();
			try (JsonReader reader = new JsonReader(resource.openAsReader())) {
				Blacklist blacklist = Blacklist.fromJson(reader);
				if (blacklist != null) {
					Neruina.LOGGER.info("Blacklist loaded for namespace: \"{}\"", id.getNamespace());
					blacklists.put(id, blacklist);
				} else {
					Neruina.LOGGER.warn("Invalid blacklist found: {}, ignoring", id);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Nullable
	public ResourceLocation getBlacklistFor(ErroredType type, ResourceLocation id) {
		for (Map.Entry<ResourceLocation, Blacklist> entry : blacklists.entrySet()) {
			ResourceLocation blacklistId = entry.getKey();
			Blacklist blacklist = entry.getValue();
			if (blacklist.isBlacklisted(type, id)) {
				return blacklistId;
			}
		}
		return null;
	}
}
