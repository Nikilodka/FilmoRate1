package com.example.filmorate1.Validators.ReleaseDate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReleaseDateValidator.class)

public @interface ReleaseDateRange {
    String message() default "Дата выпуска фильма должна быть в диапазоне от {minDate} до текущего дня";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String minDate() default "1895-12-28";
}
