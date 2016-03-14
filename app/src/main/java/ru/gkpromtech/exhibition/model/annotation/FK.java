package ru.gkpromtech.exhibition.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ru.gkpromtech.exhibition.model.Entity;

//example: @FK(entity=SomeTable.class, field="id")

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FK {
    Class<? extends Entity> entity();
    String field();
    String onDelete() default "";
    String onUpdate() default "";
}
