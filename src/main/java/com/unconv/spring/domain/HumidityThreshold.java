package com.unconv.spring.domain;

import com.unconv.spring.annotation.ValidThreshold;
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
@DiscriminatorValue("H")
@NoArgsConstructor
@AllArgsConstructor
@ValidThreshold
public class HumidityThreshold extends Threshold {
    @DecimalMin(value = "0", message = "Max value must be greater than or equal to 0")
    @DecimalMax(value = "100", message = "Max value must be less than or equal to 100")
    private double maxValue;

    @DecimalMin(value = "0", message = "Min value must be greater than or equal to 0")
    @DecimalMax(value = "100", message = "Min value must be less than or equal to 100")
    private double minValue;

    public HumidityThreshold(UUID id, double maxValue, double minValue) {
        super(id);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
}
