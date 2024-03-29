package com.bawnorton.neruina.version;

import com.bawnorton.neruina.Platform;
import com.bawnorton.neruina.version.versions.v118.V118;
import com.bawnorton.neruina.version.versions.v119.V119;
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

    /**
     * For use when sending information to the client as the client may not have neruina installed.
     */
    public static Text preTranslatedText(String key, Object... args) {
        return textOf(translatableText(key, args).getString());
    }

    public static Text formatText(Text message) {
        if (TEXT_VERSION_STRING.isVersionValid(MC_VERSION)) {
            return V119.formatText(message);
        } else {
            return V118.formatText(message);
        }
    }
}
