package com.unconv.spring.dto;

import com.unconv.spring.consts.SensorLocationType;
import java.util.UUID;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorLocationDTO {

    private UUID id;

    @NotEmpty(message = "Sensor location text cannot be empty")
    private String sensorLocationText;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "SensorLocationType cannot be null")
    private SensorLocationType sensorLocationType;
}
