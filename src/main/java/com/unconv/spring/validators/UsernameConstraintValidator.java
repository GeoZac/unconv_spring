package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidUsername;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsernameConstraintValidator implements ConstraintValidator<ValidUsername, String> {

    @Override
    public void initialize(ValidUsername arg0) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return true;
    }
}
