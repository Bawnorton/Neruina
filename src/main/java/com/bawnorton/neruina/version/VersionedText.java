package com.bawnorton.neruina.version;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import java.util.function.UnaryOperator;

public interface VersionedText {
    Text LINE_BREAK = literal("\n");
    Text SPACE = literal(" ");
    Text NERUINA_HEADER = withStyle(literal("[Neruina]: "), style -> style.withColor(Formatting.AQUA));

    static Text literal(String text) {
        /*? if >=1.19 {*/
        return Text.literal(text);
        /*? } else { *//*
        return new LiteralText(text);
        *//*? } */
    }

    static Text translatable(String key, Object... args) {
        /*? if >=1.19 {*/
        return Text.translatable(key, args);
        /*? } else { *//*
        return new TranslatableText(key, args);
        *//*? } */
    }

    static Text withStyle(Text text, UnaryOperator<Style> style) {
        if (text instanceof MutableText mutableText) {
            mutableText.styled(style);
        }
        return text;
    }

    static Text concat(Text... texts) {
        /*? if >=1.19 {*/
        MutableText text = Text.empty();
        /*? } else { *//*
        MutableText text = new LiteralText("");
        *//*? } */
        for (Text t : texts) {
            text.append(t);
        }
        return text;
    }

    static Text concatDelimited(Text delimiter, Text... texts) {
        /*? if >=1.19 {*/
        MutableText text = Text.empty();
        /*? } else { *//*
        MutableText text = new LiteralText("");
        *//*? } */
        for (int i = 0; i < texts.length; i++) {
            text.append(texts[i]);
            if(texts[i].getString().isEmpty()) {
                continue;
            }
            if (i != texts.length - 1) {
                text.append(delimiter);
            }
        }
        return text;
    }

    static Text pad(Text text) {
        /*? if >=1.19 {*/
        MutableText padded = Text.empty();
        /*? } else { *//*
        MutableText padded = new LiteralText("");
        *//*? } */
        padded.append(LINE_BREAK);
        padded.append(text);
        padded.append(LINE_BREAK);
        return padded;
    }

    static Text format(Text text) {
        return concat(NERUINA_HEADER, withStyle(text, style -> style.withColor(Formatting.RED)));
    }
}
