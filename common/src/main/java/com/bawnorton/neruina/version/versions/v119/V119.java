package com.bawnorton.neruina.version.versions.v119;

import com.bawnorton.neruina.version.Version;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class V119 {
    public static Text translatable(String key, Object... args) {
        return Text.translatable(key, args);
    }

    public static Text of(String text) {
        return Text.of(text);
    }

    public static Text formatText(Text message) {
        MutableText styled = (MutableText) message;
        styled.styled(style -> style.withColor(Formatting.RED));
        MutableText header = (MutableText) Version.textOf("[Neruina]: ");
        header.styled(style -> style.withColor(Formatting.AQUA));
        return header.append(styled);
    }
}
