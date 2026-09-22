package com.example.filmorate1.Validators.ReleaseDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDateRange, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(ReleaseDateRange constraintAnnotation) {
        this.minDate=LocalDate.parse(constraintAnnotation.minDate());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {

        LocalDate today = LocalDate.now();

        if(value.isAfter(minDate)&&value.isBefore(today)){
            return true;
        }

        return false;
    }

}
