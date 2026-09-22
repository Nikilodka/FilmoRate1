package com.example.filmorate1.Validators.BirthdayDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class BirthdayDateValidator implements ConstraintValidator<BirtdayDateRange, LocalDate> {
    private LocalDate minDate;
    private LocalDate maxDate;

    @Override
    public void initialize(BirtdayDateRange constraintAnnotation) {
        this.minDate=LocalDate.parse(constraintAnnotation.minDate());
        this.maxDate=LocalDate.parse(constraintAnnotation.maxDate());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value.isAfter(minDate) && value.isBefore(maxDate)) {
            return true;
        }
        return false;
    }
}
