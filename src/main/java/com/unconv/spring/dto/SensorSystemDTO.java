package com.unconv.spring.dto;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.UnconvUser;
import java.util.UUID;
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
public class SensorSystemDTO {

    private UUID id;

    @NotEmpty(message = "Sensor name cannot be empty")
    private String sensorName;

    private SensorLocation sensorLocation;

    @NotNull(message = "UnconvUser cannot be empty")
    private UnconvUser unconvUser;

    private long readingCount;

    private EnvironmentalReading latestReading;
}
