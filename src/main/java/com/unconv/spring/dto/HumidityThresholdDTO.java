package com.unconv.spring.dto;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HumidityThresholdDTO {

    private UUID id;

    @NotNull(message = "Min. value cannot be empty")
    private double minValue;

    @NotNull(message = "Max. value cannot be empty")
    private double maxValue;
}
