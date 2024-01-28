package com.bawnorton.neruina.annotation;

import com.bawnorton.neruina.platform.ModLoader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModLoaderMixin {
    ModLoader[] value() default {};
}
