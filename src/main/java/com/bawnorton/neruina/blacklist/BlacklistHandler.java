package com.bawnorton.neruina.blacklist;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.util.ErroredType;
import com.google.gson.stream.JsonReader;
import net.minecraft.resource.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class BlacklistHandler {
    private final Map<Identifier, Blacklist> blacklists = new HashMap<>();

    public void init(MinecraftServer server) {
        Map<Identifier, Resource> blacklistFiles = server.getResourceManager().findResources(Neruina.MOD_ID, (resource) -> resource.getPath().equals("neruina/blacklist.json"));
        for (Map.Entry<Identifier, Resource> entry : blacklistFiles.entrySet()) {
            Identifier id = entry.getKey();
            Resource resource = entry.getValue();
            try (JsonReader reader = new JsonReader(resource.getReader())) {
                Blacklist blacklist = Blacklist.fromJson(reader);
                if (blacklist != null) {
                    Neruina.LOGGER.info("Blacklist loaded for mod: \"{}\"", id);
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
    public Identifier getBlacklistFor(ErroredType type, Identifier id) {
        for (Map.Entry<Identifier, Blacklist> entry : blacklists.entrySet()) {
            Identifier blacklistId = entry.getKey();
            Blacklist blacklist = entry.getValue();
            if(blacklist.isBlacklisted(type, id)) {
                return blacklistId;
            }
        }
        return null;
    }
}
