package com.unconv.spring.persistence;

import com.unconv.spring.domain.HumidityThreshold;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HumidityThresholdRepository extends JpaRepository<HumidityThreshold, UUID> {}
