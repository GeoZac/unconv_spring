package com.unconv.spring.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unconv.spring.annotation.ValidUsername;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootTest
@TestPropertySource(
        properties = {
            "spring.mail.host=localhost",
            "spring.mail.port=1025",
            "spring.mail.username=test",
            "spring.mail.password=test"
        })
class UsernameConstraintValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.afterPropertiesSet();
        validator = factoryBean;
    }

    @Test
    void testValidUsername() {
        String validUsername = "ValidUsername";
        ValidUsernameBean bean = new ValidUsernameBean(validUsername);
        Set<ConstraintViolation<ValidUsernameBean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty(), "Expected no violations for a valid username");
    }

    @Test
    void testInvalidUsername() {
        String invalidUsername = "weak";
        ValidUsernameBean bean = new ValidUsernameBean(invalidUsername);
        Set<ConstraintViolation<ValidUsernameBean>> violations = validator.validate(bean);
        assertFalse(violations.isEmpty(), "Expected violations for an invalid username");

        // Ensure that the violation message contains the expected error message
        ConstraintViolation<ValidUsernameBean> violation = violations.iterator().next();
        assertEquals(
                "Username must be 6 or more characters in length.,Username must contain 1 or more uppercase characters.",
                violation.getMessage());
    }

    @Test
    void testNullUsername() {
        ValidUsernameBean bean = new ValidUsernameBean(null);
        Set<ConstraintViolation<ValidUsernameBean>> violations = validator.validate(bean);
        assertFalse(violations.isEmpty(), "Expected violations for a null username");
    }

    private record ValidUsernameBean(@ValidUsername String username) {}
}
