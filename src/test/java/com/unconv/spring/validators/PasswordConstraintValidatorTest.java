package com.unconv.spring.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unconv.spring.annotation.ValidPassword;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
class PasswordConstraintValidatorTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.afterPropertiesSet();
        validator = factoryBean;
    }

    @Test
    void testValidPassword() {
        String validPassword = "Passw0rd!";
        ValidPasswordBean bean = new ValidPasswordBean(validPassword);
        Set<ConstraintViolation<ValidPasswordBean>> violations = validator.validate(bean);
        assertTrue(violations.isEmpty(), "Expected no violations for a valid password");
    }

    @Test
    void testInvalidPassword() {
        String invalidPassword = "weak";
        ValidPasswordBean bean = new ValidPasswordBean(invalidPassword);
        Set<ConstraintViolation<ValidPasswordBean>> violations = validator.validate(bean);
        assertFalse(violations.isEmpty(), "Expected violations for an invalid password");

        // Ensure that the violation message contains the expected error message
        ConstraintViolation<ValidPasswordBean> violation = violations.iterator().next();
        assertEquals(
                "Password must be 6 or more characters in length.,Password must contain 1 or more uppercase characters.,Password must contain 1 or more digit characters.,Password must contain 1 or more special characters.",
                violation.getMessage());
    }

    @Test
    void testNullPassword() {
        ValidPasswordBean bean = new ValidPasswordBean(null);
        Set<ConstraintViolation<ValidPasswordBean>> violations = validator.validate(bean);
        assertFalse(violations.isEmpty(), "Expected violations for a null password");
    }

    private record ValidPasswordBean(@ValidPassword String password) {}
}
