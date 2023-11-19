package com.bawnorton.neruina.version.versions.v118;

import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.UnaryOperator;

public class MutableText extends ReflectiveClass {
    private static final Class<?> representedClass;
    private static final MethodHandle styledMethod;
    private static final MethodHandle appendMethod;

    static {
        representedClass = init("net.minecraft.text.MutableText",
                "net.minecraft.network.chat.MutableComponent",
                "net.minecraft.class_5250",
                "net.minecraft.src.C_5012_");
        styledMethod = findMethod(representedClass, List.of(
                "styled",
                "withStyle",
                "method_27694",
                "m_130938_"
        ), representedClass, UnaryOperator.class);
        appendMethod = findMethod(representedClass, List.of(
                "append",
                "method_10852",
                "m_7220_"
        ), representedClass, Text.class);
    }

    public static Text styled(Text text, UnaryOperator<Style> style) {
        try {
            return (Text) styledMethod.invoke(text, style);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static Text append(Text text, Text other) {
        try {
            return (Text) appendMethod.invoke(text, other);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
