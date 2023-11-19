package com.bawnorton.neruina.version.versions.v118;

import net.minecraft.text.Text;

import java.lang.invoke.MethodHandle;

public class TranslatableText extends ReflectiveClass {
    private static final Class<?> representedClass;
    private static final MethodHandle constructor;

    static {
        representedClass = init("net.minecraft.text.TranslatableText",
                "net.minecraft.network.chat.TranslatableComponent",
                "net.minecraft.class_2588",
                "net.minecraft.src.C_5026_");
        constructor = findConstructor(representedClass, String.class, Object[].class);
    }

    public static Text translatable(String key, Object... args) {
        try {
            return (Text) constructor.invoke(key, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
