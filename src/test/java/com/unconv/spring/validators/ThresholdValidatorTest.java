package com.unconv.spring.validators;

import static org.junit.jupiter.api.Assertions.*;

import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.dto.HumidityThresholdDTO;
import com.unconv.spring.dto.TemperatureThresholdDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThresholdValidatorTest {

    private ThresholdValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ThresholdValidator();
    }

    @Test
    void testValidHumidityThreshold() {
        HumidityThreshold humidityThreshold = new HumidityThreshold(0.5, 0.4);
        assertTrue(validator.isValid(humidityThreshold, null));
    }

    @Test
    void testValidTemperatureThreshold() {
        TemperatureThreshold temperatureThreshold = new TemperatureThreshold(20.0, 25.0);
        assertFalse(validator.isValid(temperatureThreshold, null));
    }

    @Test
    void testValidHumidityThresholdDTO() {
        HumidityThresholdDTO humidityThresholdDTO = new HumidityThresholdDTO(null, 0.5, 0.7);
        assertTrue(validator.isValid(humidityThresholdDTO, null));
    }

    @Test
    void testValidTemperatureThresholdDTO() {
        TemperatureThresholdDTO temperatureThresholdDTO =
                new TemperatureThresholdDTO(null, 20.0, 25.0);
        assertTrue(validator.isValid(temperatureThresholdDTO, null));
    }

    @Test
    void testInvalidHumidityThreshold() {
        HumidityThreshold humidityThreshold = new HumidityThreshold(0.4, 0.5);
        assertFalse(validator.isValid(humidityThreshold, null));
    }

    @Test
    void testInvalidTemperatureThreshold() {
        TemperatureThreshold temperatureThreshold = new TemperatureThreshold(25.0, 20.0);
        assertTrue(validator.isValid(temperatureThreshold, null));
    }

    @Test
    void testInvalidHumidityThresholdDTO() {
        HumidityThresholdDTO humidityThresholdDTO = new HumidityThresholdDTO(null, 0.7, 0.5);
        assertFalse(validator.isValid(humidityThresholdDTO, null));
    }

    @Test
    void testInvalidTemperatureThresholdDTO() {
        TemperatureThresholdDTO temperatureThresholdDTO =
                new TemperatureThresholdDTO(null, 25.0, 20.0);
        assertFalse(validator.isValid(temperatureThresholdDTO, null));
    }

    @Test
    void testInvalidObjectType() {
        Object someOtherObject = new Object();
        assertFalse(validator.isValid(someOtherObject, null));
    }
}
