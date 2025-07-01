package com.unconv.spring.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.unconv.spring.validators.UsernameConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation to validate that a username meets specific criteria.
 *
 * <p>This annotation can be used on fields or annotation types. It uses the {@link
 * UsernameConstraintValidator} class to perform the validation logic.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @ValidUsername
 * private String username;
 * }</pre>
 *
 * <p>The default error message is "Invalid Username", but this can be overridden by specifying a
 * custom message.
 *
 * <p>This annotation can also be grouped and can carry metadata information through the payload.
 *
 * @see UsernameConstraintValidator
 */
@Documented
@Constraint(validatedBy = UsernameConstraintValidator.class)
@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ValidUsername {

    /**
     * Specifies the error message that will be returned if the username is invalid.
     *
     * @return the error message
     */
    String message() default "Invalid Username";

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
