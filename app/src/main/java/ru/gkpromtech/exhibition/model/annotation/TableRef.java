package ru.gkpromtech.exhibition.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface TableRef {
    String name();
    boolean tr() default false;
    String trid() default "";
    boolean sync() default true;
}
