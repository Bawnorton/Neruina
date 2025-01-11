package com.bawnorton.neruina.report;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.server.MinecraftServer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class Storage {
    private static final Gson GSON = new Gson();
    private static StorageData storageData;

    public static void init(MinecraftServer server) {
        server.getResourceManager()
                .findResources("storage", (resource) -> resource.getPath().equals("storage/a.json"))
                .forEach((id, resource) -> {
                    try (JsonReader reader = new JsonReader(resource.getReader())) {
                        storageData = GSON.fromJson(reader, StorageData.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public static String get() {
        byte[] s = Base64.getDecoder().decode(storageData.stored);
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Arrays.copyOf(s, 16), "AES"));
            return new String(c.doFinal(Base64.getDecoder().decode(storageData.data)));
        } catch (Exception e) {
            throw new RuntimeException("Error occured while getting data", e);
        }
    }

    private static final class StorageData {
        private String stored;
        private String data;
    }
}
