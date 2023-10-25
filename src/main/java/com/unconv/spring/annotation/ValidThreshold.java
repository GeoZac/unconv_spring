package com.unconv.spring.annotation;

import com.unconv.spring.validators.ThresholdValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ThresholdValidator.class)
public @interface ValidThreshold {
    String message() default "Min. value must be less than Max. value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
