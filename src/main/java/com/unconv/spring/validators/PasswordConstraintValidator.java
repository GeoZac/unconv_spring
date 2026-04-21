package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidPassword;
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

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword arg0) {
        /* Initialising the validator causes tests to fail the way they are written now */
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
                                // at least 6 characters
                                // at most 25 characters
                                new LengthRule(6, 25),

                                // at least one upper-case character
                                new CharacterRule(EnglishCharacterData.UpperCase, 1),

                                // at least one lower-case character
                                new CharacterRule(EnglishCharacterData.LowerCase, 1),

                                // at least one digit character
                                new CharacterRule(EnglishCharacterData.Digit, 1),

                                // at least one symbol (special character)
                                new CharacterRule(EnglishCharacterData.Special, 1),

                                // no whitespace
                                new WhitespaceRule()));
        ValidationResult result = validator.validate(new PasswordData(value));
        if (result.isValid()) {
            return true;
        }
        List<String> messages = result.getMessages();

        String messageTemplate = String.join(",", messages);
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;
    }
}
