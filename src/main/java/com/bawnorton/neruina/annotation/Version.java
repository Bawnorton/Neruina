package com.bawnorton.neruina.annotation;

public @interface Version {
    String min() default "";
    String max() default "";
}
