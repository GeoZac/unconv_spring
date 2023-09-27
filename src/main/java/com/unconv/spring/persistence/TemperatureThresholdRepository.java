package com.unconv.spring.persistence;

import com.unconv.spring.domain.TemperatureThreshold;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureThresholdRepository extends JpaRepository<TemperatureThreshold, UUID> {}
