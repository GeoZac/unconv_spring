package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidThreshold;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.dto.HumidityThresholdDTO;
import com.unconv.spring.dto.TemperatureThresholdDTO;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for validating threshold values. This class implements {@link ConstraintValidator} to
 * validate threshold values annotated with {@link ValidThreshold}.
 */
public class ThresholdValidator implements ConstraintValidator<ValidThreshold, Object> {

    /**
     * Initializes the validator. Currently, this method does not perform any initialization.
     *
     * @param constraintAnnotation The annotation instance for the constraint.
     */
    @Override
    public void initialize(ValidThreshold constraintAnnotation) {
        /* Initialising the validator causes tests to fail the way they are written now */
    }

    /**
     * Validates the threshold value. This method checks if the minimum value is less than the
     * maximum value for the provided threshold object.
     *
     * @param value The value to be validated.
     * @param context The context in which the constraint is evaluated.
     * @return {@code true} if the minimum value is less than the maximum value, {@code false}
     *     otherwise.
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof HumidityThreshold humidityThreshold) {

            double minValue = humidityThreshold.getMinValue();
            double maxValue = humidityThreshold.getMaxValue();

            return minValue < maxValue;
        } else if (value instanceof TemperatureThreshold temperatureThreshold) {

            double minValue = temperatureThreshold.getMinValue();
            double maxValue = temperatureThreshold.getMaxValue();

            return minValue < maxValue;

        } else if (value instanceof HumidityThresholdDTO humidityThresholdDTO) {

            double minValue = humidityThresholdDTO.getMinValue();
            double maxValue = humidityThresholdDTO.getMaxValue();

            return minValue < maxValue;
        } else if (value instanceof TemperatureThresholdDTO temperatureThresholdDTO) {

            double minValue = temperatureThresholdDTO.getMinValue();
            double maxValue = temperatureThresholdDTO.getMaxValue();

            return minValue < maxValue;
        }

        return false;
    }
}
