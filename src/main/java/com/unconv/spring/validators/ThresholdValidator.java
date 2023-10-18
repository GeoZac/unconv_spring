package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidThreshold;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ThresholdValidator implements ConstraintValidator<ValidThreshold, Object> {

    @Override
    public void initialize(ValidThreshold constraintAnnotation) {
        /* Initialising the validator causes tests to fail the way they are written now */
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return false;
    }
}
