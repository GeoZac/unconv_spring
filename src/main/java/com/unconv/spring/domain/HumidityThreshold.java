package com.unconv.spring.domain;

import com.unconv.spring.domain.shared.Threshold;
import java.util.UUID;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("H")
@NoArgsConstructor
public class HumidityThreshold extends Threshold {
    public HumidityThreshold(UUID id, double maxValue, double minValue) {
        super(id, maxValue, minValue);
    }
}
