package com.bawnorton.neruina.report;

import com.bawnorton.neruina.Neruina;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.server.MinecraftServer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

/*? if <1.19 { *//*
import net.minecraft.resource.Resource;
import java.io.InputStream;
import java.io.InputStreamReader;
*//*? } */

public class Storage {
    private static final Gson GSON = new Gson();
    private static StorageData storageData;

    public static void init(MinecraftServer server) {
        /*? if >=1.19 { */
        server.getResourceManager().findResources(Neruina.MOD_ID, (resource) -> resource.getPath().equals("neruina/storage.json")).forEach((id, resource) -> {
            try (JsonReader reader = new JsonReader(resource.getReader())) {
                storageData = GSON.fromJson(reader, StorageData.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        /*? } else { *//*
        server.getResourceManager().findResources(Neruina.MOD_ID, path -> path.equals("storage.json")).forEach(path -> {
            try (Resource resource = server.getResourceManager().getResource(path)) {
                InputStream io = resource.getInputStream();
                JsonReader reader = new JsonReader(new InputStreamReader(io));
                storageData = GSON.fromJson(reader, StorageData.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        *//*? }*/
    }

    public static String get() {
        byte[] s = Base64.getDecoder().decode(storageData.stored);try {Cipher c = Cipher.getInstance("AES");c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Arrays.copyOf(s, 16), "AES"));return new String(c.doFinal(Base64.getDecoder().decode(storageData.data)));} catch (Exception e) {throw new RuntimeException("Error occured while getting data", e);}
    }

    @SuppressWarnings("FieldMayBeFinal")
    private static final class StorageData {
        private String stored;
        private String data;

        private StorageData(String stored, String data) {
            this.stored = stored;
            this.data = data;
        }
    }
}
