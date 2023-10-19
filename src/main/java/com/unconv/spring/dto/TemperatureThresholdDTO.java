package com.unconv.spring.dto;

import com.unconv.spring.annotation.ValidThreshold;
import java.util.UUID;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
