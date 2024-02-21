package com.bawnorton.neruina.util;

import java.lang.reflect.Method;

public class Reflection {
    public static Method findMethod(Class<?> clazz, String name) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        if(clazz.getSuperclass() != null) {
            return findMethod(clazz.getSuperclass(), name);
        }
        return null;
    }
}
