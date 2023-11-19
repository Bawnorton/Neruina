package com.bawnorton.neruina.version.versions.v118;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public abstract class ReflectiveClass {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected static Class<?> init(String... possibleClassNames) {
        for (String className : possibleClassNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new RuntimeException("Could not find class for " + possibleClassNames[0]);
    }

    protected static MethodHandle findMethod(Class<?> owner, List<String> possibleNames, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType methodType = MethodType.methodType(returnType, parameterTypes);
        for (String methodName : possibleNames) {
            try {
                return lookup.findVirtual(owner, methodName, methodType);
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
            }
        }
        throw new RuntimeException("Could not find method for " + possibleNames.get(0));
    }

    protected static MethodHandle findConstructor(Class<?> owner, Class<?>... parameterTypes) {
        MethodType methodType = MethodType.methodType(void.class, parameterTypes);
        try {
            return lookup.findConstructor(owner, methodType);
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        }
        throw new RuntimeException("Could not find constructor for " + owner.getName());
    }
}
