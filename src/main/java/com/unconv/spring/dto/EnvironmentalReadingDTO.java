package com.unconv.spring.dto;

import static com.unconv.spring.consts.MessageConstants.ENVT_VALID_SENSOR_SYSTEM;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.base.BaseEnvironmentalReadingDTO;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A specialized class that extends {@link BaseEnvironmentalReadingDTO} to represent environmental
 * readings within the context of a sensor system. It includes additional attributes specific to
 * sensor readings.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalReadingDTO extends BaseEnvironmentalReadingDTO {
    private UUID id;

    @NotNull(message = ENVT_VALID_SENSOR_SYSTEM)
    private SensorSystem sensorSystem;

    /**
     * Constructs an {@link EnvironmentalReadingDTO} with the specified parameters.
     *
     * @param uuid the unique identifier for the environmental reading
     * @param temperature the temperature value of the reading
     * @param humidity the humidity value of the reading
     * @param timeStamp the timestamp of the reading
     * @param sensorSystem the sensor system associated with the reading
     */
    public EnvironmentalReadingDTO(
            UUID uuid,
            long temperature,
            long humidity,
            OffsetDateTime timeStamp,
            SensorSystem sensorSystem) {
        super(temperature, humidity, timeStamp);
        this.id = uuid;
        this.sensorSystem = sensorSystem;
    }
}
