package com.unconv.spring.persistence;

import com.unconv.spring.domain.shared.Threshold;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @deprecated This interface is deprecated and will be removed in future versions. Please use
 *     {@link HumidityThresholdRepository} or {@link TemperatureThresholdRepository} instead.
 */
@Deprecated(forRemoval = true)
public interface ThresholdRepository extends JpaRepository<Threshold, UUID> {}
