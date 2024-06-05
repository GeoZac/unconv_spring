package com.unconv.spring.persistence;

import com.unconv.spring.domain.TemperatureThreshold;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link TemperatureThreshold} entities. Extends {@link
 * JpaRepository} to inherit basic CRUD functionality.
 */
public interface TemperatureThresholdRepository extends JpaRepository<TemperatureThreshold, UUID> {}
