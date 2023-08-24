package com.unconv.spring.dto.base;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseEnvironmentalReadingDTO {

    @DecimalMin(value = "-9999.000", inclusive = true)
    @DecimalMax(value = "9999.000", inclusive = true)
    @NotNull(message = "Temperature cannot be empty")
    private double temperature;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.00", inclusive = true)
    @NotNull(message = "Humidity cannot be empty")
    private double humidity;

    @PastOrPresent(message = "Readings has to be in past or present")
    private OffsetDateTime timestamp;

    public void setTimestamp() {
        this.timestamp = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
    }

    public String toCSVString() {
        return this.temperature + "," + this.humidity + "," + this.timestamp;
    }
}
