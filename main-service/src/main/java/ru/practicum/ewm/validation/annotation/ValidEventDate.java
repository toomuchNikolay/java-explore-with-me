package ru.practicum.ewm.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.ewm.validation.validator.ValidEventDateValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Constraint(validatedBy = ValidEventDateValidator.class)
public @interface ValidEventDate {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int hours() default 0;
}
