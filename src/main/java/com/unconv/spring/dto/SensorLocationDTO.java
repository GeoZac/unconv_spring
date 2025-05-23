package com.unconv.spring.dto;

import com.unconv.spring.enums.SensorLocationType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Data Transfer Object for SensorLocation entity */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorLocationDTO {

    private UUID id;

    @NotEmpty(message = "Sensor location text cannot be empty")
    private String sensorLocationText;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @NotNull(message = "SensorLocationType cannot be null")
    private SensorLocationType sensorLocationType;
}
