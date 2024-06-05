package com.unconv.spring.persistence;

import com.unconv.spring.domain.HumidityThreshold;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link HumidityThreshold} entities. Extends {@link
 * JpaRepository} to inherit basic CRUD functionality.
 */
public interface HumidityThresholdRepository extends JpaRepository<HumidityThreshold, UUID> {}
