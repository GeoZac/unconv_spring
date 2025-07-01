package com.unconv.spring.dto.base;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The base class for representing environmental readings, encapsulating common attributes such as
 * temperature, humidity, and timestamp. This class provides a foundation for specialized
 * environmental reading classes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseEnvironmentalReadingDTO {

    @DecimalMin(value = "-9999.000")
    @DecimalMax(value = "9999.000")
    @NotNull(message = "Temperature cannot be empty")
    private double temperature;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.00")
    @NotNull(message = "Humidity cannot be empty")
    private double humidity;

    @PastOrPresent(message = "Readings has to be in past or present")
    private OffsetDateTime timestamp;

    /**
     * Sets the timestamp of the environmental reading to the current UTC time. Uses {@link
     * OffsetDateTime} to ensure the timestamp includes timezone offset information.
     */
    public void setTimestamp() {
        this.timestamp = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
    }

    /**
     * Converts the environmental reading data to a CSV-formatted string.
     *
     * @return a string representing the environmental reading in CSV format:
     *     temperature,humidity,timestamp
     */
    public String toCSVString() {
        return this.temperature + "," + this.humidity + "," + this.timestamp;
    }
}
