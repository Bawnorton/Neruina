package com.bawnorton.neruina.version;

import com.bawnorton.neruina.Platform;
import com.bawnorton.neruina.version.versions.V118;
import com.bawnorton.neruina.version.versions.V119;
import net.minecraft.text.Text;

public abstract class Version {
    private static final String MC_VERSION = Platform.getMinecraftVersion();
    private static final VersionString TEXT_VERSION_STRING = new VersionString(">=1.19");

    public static Text translatableText(String key, Object... args) {
        if (TEXT_VERSION_STRING.isVersionValid(MC_VERSION)) {
            return V119.translatable(key, args);
        } else {
            return V118.translatable(key, args);
        }
    }

    public static Text textOf(String text) {
        if (TEXT_VERSION_STRING.isVersionValid(MC_VERSION)) {
            return V119.of(text);
        } else {
            return V118.of(text);
        }
    }
}
