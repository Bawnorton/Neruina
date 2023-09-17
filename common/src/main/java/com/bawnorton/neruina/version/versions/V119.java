package com.bawnorton.neruina.version.versions;

import net.minecraft.text.Text;

public abstract class V119 {
    public static Text translatable(String key, Object... args) {
        return Text.translatable(key, args);
    }

    public static Text of(String text) {
        return Text.of(text);
    }
}
