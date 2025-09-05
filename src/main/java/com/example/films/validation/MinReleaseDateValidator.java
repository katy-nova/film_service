package com.example.films.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MinReleaseDateValidator implements ConstraintValidator<MinReleaseDate, LocalDate> {
    private static final LocalDate EARLIEST_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (localDate == null) {
            return true;
        }
        return localDate.isAfter(EARLIEST_DATE);
    }
}
