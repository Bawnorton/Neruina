package com.bawnorton.neruina.version.versions;

import com.bawnorton.neruina.Neruina;
import net.minecraft.text.Text;

import java.lang.reflect.InvocationTargetException;

public abstract class V118 {
    public static Text translatable(String key, Object... args) {
        try {
            Class<?> translatableTextClass = Class.forName("net.minecraft.text.TranslatableText");
            if(args.length == 0) return (Text) translatableTextClass.getConstructor(String.class).newInstance(key);
            else return (Text) translatableTextClass.getConstructor(String.class, Object[].class).newInstance(key, args);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            Neruina.LOGGER.error("Failed to get translatable text for key " + key + "!", e);
            return null;
        }
    }

    public static Text of(String text) {
        try {
            Class<?> textClass = Class.forName("net.minecraft.text.LiteralText");
            return (Text) textClass.getConstructor(String.class).newInstance(text);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            Neruina.LOGGER.error("Failed to get text of " + text + "!", e);
            return null;
        }
    }
}
