package com.bawnorton.neruina.util.annotation;

public @interface Version {
	String min() default "";

	String max() default "";
}
