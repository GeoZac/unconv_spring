package com.unconv.spring.domain;

import com.unconv.spring.domain.shared.Threshold;
import java.util.UUID;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("T")
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureThreshold extends Threshold {
    @DecimalMin(value = "-9999", message = "Max value must be greater than or equal to -9999")
    @DecimalMax(value = "9999", message = "Max value must be less than or equal to 9999")
    private double maxValue;

    @DecimalMin(value = "-9999", message = "Min value must be greater than or equal to -9999")
    @DecimalMax(value = "9999", message = "Min value must be less than or equal to 9999")
    private double minValue;

    public TemperatureThreshold(UUID id, double maxValue, double minValue) {
        super(id);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
}
