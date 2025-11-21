package com.rentkar.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureOrPresentValidator.class)
@Documented
public @interface FutureOrPresent {
    String message() default "Date must not be in the past";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
