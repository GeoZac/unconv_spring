package com.unconv.spring.annotation;

import com.unconv.spring.validators.ThresholdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to validate that a minimum value is less than a maximum value within a class.
 *
 * <p>This annotation can be used on types (classes or interfaces). It uses the {@link
 * ThresholdValidator} class to perform the validation logic.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @ValidThreshold
 * public class Range {
 *     private int min;
 *     private int max;
 *
 *     // getters and setters
 * }
 * }</pre>
 *
 * <p>The default error message is "Min. value must be less than Max. value", but this can be
 * overridden by specifying a custom message.
 *
 * <p>This annotation can also be grouped and can carry metadata information through the payload.
 *
 * @see ThresholdValidator
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ThresholdValidator.class)
public @interface ValidThreshold {

    /**
     * Specifies the error message that will be returned if the validation fails.
     *
     * @return the error message
     */
    String message() default "Min. value must be less than Max. value";

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
