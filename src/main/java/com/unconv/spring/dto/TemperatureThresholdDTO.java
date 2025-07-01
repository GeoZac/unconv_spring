package com.unconv.spring.dto;

import com.unconv.spring.annotation.ValidThreshold;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Data Transfer Object for TemperatureThreshold entity */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidThreshold
public class TemperatureThresholdDTO {

    private UUID id;

    @NotNull(message = "Min. value cannot be empty")
    @DecimalMin(value = "-9999", message = "Min value must be greater than or equal to -9999")
    @DecimalMax(value = "9999", message = "Min value must be less than or equal to 9999")
    private double minValue;

    @NotNull(message = "Max. value cannot be empty")
    @DecimalMin(value = "-9999", message = "Max value must be greater than or equal to -9999")
    @DecimalMax(value = "9999", message = "Max value must be less than or equal to 9999")
    private double maxValue;
}
