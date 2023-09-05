package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidPassword;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword arg0) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return true;
    }
}
