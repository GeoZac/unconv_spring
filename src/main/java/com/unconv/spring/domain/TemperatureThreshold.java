package com.unconv.spring.domain;

import com.unconv.spring.domain.shared.Threshold;
import java.util.UUID;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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
    private double maxValue;

    private double minValue;

    public TemperatureThreshold(UUID id, double maxValue, double minValue) {
        super(id);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
}
