package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import org.passay.DefaultPasswordValidator;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.ValidationResult;
import org.passay.data.EnglishCharacterData;
import org.passay.resolver.ResourceBundleMessageResolver;
import org.passay.rule.CharacterRule;
import org.passay.rule.LengthRule;
import org.passay.rule.WhitespaceRule;

public class UsernameConstraintValidator implements ConstraintValidator<ValidUsername, String> {

    @Override
    public void initialize(ValidUsername arg0) {
        /* Initialising the validator here causes tests to fail the way they are written now */
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        PasswordValidator validator =
                new DefaultPasswordValidator(
                        new ResourceBundleMessageResolver(),
                        Arrays.asList(
                                // At least 6 characters
                                // At most 25 characters
                                new LengthRule(6, 25),

                                // At least one upper-case character
                                new CharacterRule(EnglishCharacterData.UpperCase, 1),

                                // No whitespace
                                new WhitespaceRule()));
        ValidationResult result = validator.validate(new PasswordData(value));
        if (result.isValid()) {
            return true;
        }
        List<String> messages = result.getMessages();

        String messageTemplate = String.join(",", messages).replace("Password", "Username");
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;
    }
}
