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
@DiscriminatorValue("H")
@NoArgsConstructor
@AllArgsConstructor
public class HumidityThreshold extends Threshold {
    private double maxValue;

    private double minValue;

    public HumidityThreshold(UUID id, double maxValue, double minValue) {
        super(id);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
}
