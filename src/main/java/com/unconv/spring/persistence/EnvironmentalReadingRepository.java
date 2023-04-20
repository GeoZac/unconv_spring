package com.unconv.spring.persistence;

import com.unconv.spring.domain.EnvironmentalReading;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnvironmentalReadingRepository extends JpaRepository<EnvironmentalReading, UUID> {}
