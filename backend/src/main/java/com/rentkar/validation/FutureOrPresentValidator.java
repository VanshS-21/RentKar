package com.rentkar.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class FutureOrPresentValidator implements ConstraintValidator<FutureOrPresent, LocalDate> {
    
    @Override
    public void initialize(FutureOrPresent constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null values are handled by @NotNull
        }
        return !value.isBefore(LocalDate.now());
    }
}
