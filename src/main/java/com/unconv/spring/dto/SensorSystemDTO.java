package com.unconv.spring.dto;

import com.unconv.spring.consts.SensorStatus;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.base.BaseEnvironmentalReadingDTO;
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

    public SensorSystemDTO(
            UUID uuid, String sensorName, SensorLocation sensorLocation, UnconvUser unconvUser) {
        this.id = uuid;
        this.sensorName = sensorName;
        this.sensorLocation = sensorLocation;
        this.unconvUser = unconvUser;

        // Set defaults for backward compatibility
        this.description = null;
        this.deleted = false;
        this.sensorStatus = SensorStatus.ACTIVE;
    }

    private UUID id;

    @NotEmpty(message = "Sensor name cannot be empty")
    private String sensorName;

    private String description;

    @NotNull(message = "Deleted status cannot be null for Sensor")
    private boolean deleted = false;

    @NotNull(message = "Sensor status cannot be null")
    private SensorStatus sensorStatus;

    private SensorLocation sensorLocation;

    @NotNull(message = "UnconvUser cannot be empty")
    private UnconvUser unconvUser;

    private long readingCount;

    private BaseEnvironmentalReadingDTO latestReading;
}
