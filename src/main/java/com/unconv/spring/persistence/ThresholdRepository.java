package com.unconv.spring.persistence;

import com.unconv.spring.domain.shared.Threshold;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdRepository extends JpaRepository<Threshold, UUID> {}
