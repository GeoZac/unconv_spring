package com.unconv.spring.validators;

import com.unconv.spring.annotation.ValidThreshold;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.dto.HumidityThresholdDTO;
import com.unconv.spring.dto.TemperatureThresholdDTO;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ThresholdValidator implements ConstraintValidator<ValidThreshold, Object> {

    @Override
    public void initialize(ValidThreshold constraintAnnotation) {
        /* Initialising the validator causes tests to fail the way they are written now */
    }

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
