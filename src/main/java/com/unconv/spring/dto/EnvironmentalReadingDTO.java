package com.unconv.spring.dto;

import static com.unconv.spring.consts.MessageConstants.ENVT_VALID_SENSOR_SYSTEM;

import com.unconv.spring.domain.SensorSystem;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
public class EnvironmentalReadingDTO {
    private UUID id;

    @DecimalMin(value = "-9999.000")
    @DecimalMax(value = "9999.000")
    @NotNull(message = "Temperature cannot be empty")
    private double temperature;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.00")
    @NotNull(message = "Humidity cannot be empty")
    private double humidity;

    private OffsetDateTime timestamp;

    @NotNull(message = ENVT_VALID_SENSOR_SYSTEM)
    private SensorSystem sensorSystem;

    public void setTimestamp() {
        this.timestamp = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
    }

    public String toCSVString() {
        return this.temperature + "," + this.humidity + "," + this.timestamp;
    }
}
