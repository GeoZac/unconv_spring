package com.unconv.spring.dto;

import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.base.BaseEnvironmentalReadingDTO;
import com.unconv.spring.enums.SensorStatus;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The `SensorSystemDTO` class encapsulates information about a sensor system. It aggregates
 * including ID, name, status, location, user, thresholds, and the latest environmental reading.
 *
 * <p>This class includes a reference to the `{@link BaseEnvironmentalReadingDTO}` class, which
 * serves as the foundation for environmental readings.
 */
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

    private HumidityThreshold humidityThreshold;

    private TemperatureThreshold temperatureThreshold;

    private long readingCount;

    private BaseEnvironmentalReadingDTO latestReading;
}
