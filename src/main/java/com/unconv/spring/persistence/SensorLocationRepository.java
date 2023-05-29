package com.unconv.spring.persistence;

import com.unconv.spring.domain.SensorLocation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorLocationRepository extends JpaRepository<SensorLocation, UUID> {}
