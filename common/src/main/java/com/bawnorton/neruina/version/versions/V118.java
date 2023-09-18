package com.bawnorton.neruina.version.versions;

import net.minecraft.text.Text;

import java.util.List;

public abstract class V118 {
    private static Class<?> literalTextClass;
    private static Class<?> translatableTextClass;

    static {
        List<String> possibleLiteralTextClassNames = List.of(
                "net.minecraft.text.LiteralText",
                "net.minecraft.network.chat.TextComponent",
                "net.minecraft.class_2585",
                "net.minecraft.src.C_5025_"
        );
        List<String> possibleTranslatableTextClassNames = List.of(
                "net.minecraft.text.TranslatableText",
                "net.minecraft.network.chat.TranslatableComponent",
                "net.minecraft.class_2588",
                "net.minecraft.src.C_5026_"
        );
        for (String className : possibleLiteralTextClassNames) {
            try {
                literalTextClass = Class.forName(className);
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }
        for (String className : possibleTranslatableTextClassNames) {
            try {
                translatableTextClass = Class.forName(className);
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }

        if(literalTextClass == null) throw new RuntimeException("Could not find literal text class");
        if(translatableTextClass == null) throw new RuntimeException("Could not find translatable text class");
    }

    public static Text translatable(String key, Object... args) {
        try {
            if(args.length == 0) return (Text) translatableTextClass.getConstructor(String.class).newInstance(key);
            else return (Text) translatableTextClass.getConstructor(String.class, Object[].class).newInstance(key, args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get translatable text of " + key + "!", e);
        }
    }

    public static Text of(String text) {
        try {
            return (Text) literalTextClass.getConstructor(String.class).newInstance(text);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get text of " + text + "!", e);
        }
    }
}
