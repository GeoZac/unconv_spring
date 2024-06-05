package com.unconv.spring.persistence;

import com.unconv.spring.domain.SensorLocation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link SensorLocation} entities. Extends {@link JpaRepository}
 * to inherit basic CRUD functionality.
 */
public interface SensorLocationRepository extends JpaRepository<SensorLocation, UUID> {}
