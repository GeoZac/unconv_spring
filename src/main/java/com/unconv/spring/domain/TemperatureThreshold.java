package com.unconv.spring.domain;

import com.unconv.spring.annotation.ValidThreshold;
import com.unconv.spring.domain.shared.Threshold;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class represents a specific type of threshold for temperature values, extending the general
 * {@link Threshold} class. "T" denotes temperature
 */
@Entity
@Getter
@Setter
@DiscriminatorValue("T")
@NoArgsConstructor
@AllArgsConstructor
@ValidThreshold
public class TemperatureThreshold extends Threshold {
    @DecimalMin(value = "-9999", message = "Max value must be greater than or equal to -9999")
    @DecimalMax(value = "9999", message = "Max value must be less than or equal to 9999")
    private double maxValue;

    @DecimalMin(value = "-9999", message = "Min value must be greater than or equal to -9999")
    @DecimalMax(value = "9999", message = "Min value must be less than or equal to 9999")
    private double minValue;

    /**
     * Constructs a new {@code TemperatureThreshold} with the specified ID, maximum value, and
     * minimum value.
     *
     * @param id the unique identifier for this threshold
     * @param maxValue the maximum allowable value for the humidity threshold, must be between -9999
     *     and 9999
     * @param minValue the minimum allowable value for the humidity threshold, must be between -9999
     *     and 9999
     */
    public TemperatureThreshold(UUID id, double maxValue, double minValue) {
        super(id);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
}
