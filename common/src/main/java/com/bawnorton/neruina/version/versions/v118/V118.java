package com.bawnorton.neruina.version.versions.v118;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public abstract class V118 {
    public static Text translatable(String key, Object... args) {
        return TranslatableText.translatable(key, args);
    }

    public static Text of(String text) {
        return LiteralText.of(text);
    }

    public static Text formatText(Text message) {
        message = MutableText.styled(message, style -> style.withColor(Formatting.RED));
        Text header = LiteralText.of("[Neruina]: ");
        header = MutableText.styled(header, style -> style.withColor(Formatting.AQUA));
        return MutableText.append(header, message);
    }
}
