package com.bawnorton.neruina.version.versions.v118;

import net.minecraft.text.Text;

import java.lang.invoke.MethodHandle;

public class LiteralText extends ReflectiveClass {
    private static final Class<?> representedClass;
    private static final MethodHandle constructor;

    static {
        representedClass = init("net.minecraft.text.LiteralText",
                "net.minecraft.network.chat.TextComponent",
                "net.minecraft.class_2585",
                "net.minecraft.src.C_5025_");
        constructor = findConstructor(representedClass, String.class);
    }

    public static Text of(String text) {
        try {
            return (Text) constructor.invoke(text);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
