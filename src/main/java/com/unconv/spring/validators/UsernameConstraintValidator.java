package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidUsername;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

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
                new PasswordValidator(
                        Arrays.asList(
                                // At least 6 characters
                                // At most 25 characters
                                new LengthRule(6, 25),

                                // At least one upper-case character
                                new CharacterRule(EnglishCharacterData.UpperCase, 1),

                                // No whitespace
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
