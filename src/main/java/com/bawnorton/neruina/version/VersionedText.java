package com.bawnorton.neruina.version;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.function.UnaryOperator;

public interface VersionedText {
    /*? if >=1.19 {*/
    Text LINE_BREAK = Text.literal("\n");
    /*? } else { *//*
    Text LINE_BREAK = new LiteralText("\n");
    *//*? } */

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
            if (i != texts.length - 1) {
                text.append(delimiter);
            }
        }
        return text;
    }
}
