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

/** Data Transfer Object for HumidityThreshold entity */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidThreshold
public class HumidityThresholdDTO {

    private UUID id;

    @NotNull(message = "Min. value cannot be empty")
    @DecimalMin(value = "0", message = "Min value must be greater than or equal to 0")
    @DecimalMax(value = "100", message = "Min value must be less than or equal to 100")
    private double minValue;

    @NotNull(message = "Max. value cannot be empty")
    @DecimalMin(value = "0", message = "Max value must be greater than or equal to 0")
    @DecimalMax(value = "100", message = "Max value must be less than or equal to 100")
    private double maxValue;
}
