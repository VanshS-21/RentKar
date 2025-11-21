package com.rentkar.validation;

import com.rentkar.dto.CreateBorrowRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, CreateBorrowRequestDTO> {
    
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(CreateBorrowRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getBorrowDate() == null || dto.getReturnDate() == null) {
            return true; // null values are handled by @NotNull
        }
        
        return dto.getReturnDate().isAfter(dto.getBorrowDate());
    }
}
