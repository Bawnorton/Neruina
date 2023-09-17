package com.bawnorton.neruina.version.versions;

import net.minecraft.text.Text;

import java.lang.reflect.InvocationTargetException;

public abstract class V118 {
    public static Text translatable(String key, Object... args) {
        try {
            Class<?> translatableTextClass = Class.forName("net.minecraft.class_2588");
            if(args.length == 0) return (Text) translatableTextClass.getConstructor(String.class).newInstance(key);
            else return (Text) translatableTextClass.getConstructor(String.class, Object[].class).newInstance(key, args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get translatable text of " + key + "!", e);
        }
    }

    public static Text of(String text) {
        try {
            Class<?> textClass = Class.forName("net.minecraft.class_2585");
            return (Text) textClass.getConstructor(String.class).newInstance(text);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get text of " + text + "!", e);
        }
    }
}
