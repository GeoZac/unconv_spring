package com.unconv.spring.persistence;

import com.unconv.spring.domain.EnvironmentalReading;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentalReadingRepository extends JpaRepository<EnvironmentalReading, Long> {}
