package com.unconv.spring.persistence;

import com.unconv.spring.domain.SensorLocation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SensorLocationRepository extends JpaRepository<SensorLocation, UUID> {}
