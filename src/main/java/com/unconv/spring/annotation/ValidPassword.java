package com.unconv.spring.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.unconv.spring.validators.PasswordConstraintValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * An annotation to validate that a password meets specific criteria.
 *
 * <p>This annotation can be used on fields or annotation types. It uses the {@link
 * PasswordConstraintValidator} class to perform the validation logic.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @ValidPassword
 * private String password;
 * }</pre>
 *
 * <p>The default error message is "Invalid Password", but this can be overridden by specifying a
 * custom message.
 *
 * <p>This annotation can also be grouped and can carry metadata information through the payload.
 *
 * @see PasswordConstraintValidator
 */
@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ValidPassword {

    /**
     * Specifies the error message that will be returned if the password is invalid.
     *
     * @return the error message
     */
    String message() default "Invalid Password";

    /**
     * Allows the specification of validation groups, to which this constraint belongs. This must
     * default to an empty array.
     *
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Can be used by clients of the Bean Validation API to assign custom payload objects to a
     * constraint. This attribute must default to an empty array.
     *
     * @return the custom payload
     */
    Class<? extends Payload>[] payload() default {};
}
