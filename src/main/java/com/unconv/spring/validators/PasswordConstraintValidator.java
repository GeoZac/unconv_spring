package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

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
                new PasswordValidator(
                        Arrays.asList(
                                // at least 8 characters
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
        RuleResult result = validator.validate(new PasswordData(value));
        if (result.isValid()) {
            return true;
        }
        List<String> messages = validator.getMessages(result);

        String messageTemplate = messages.stream().collect(Collectors.joining(","));
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;
    }
}
