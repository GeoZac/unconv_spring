package com.unconv.spring.persistence;

import com.unconv.spring.domain.SensorSystem;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorSystemRepository extends JpaRepository<SensorSystem, UUID> {
    Page<SensorSystem> findAllByUnconvUserId(UUID unconvUserId, Pageable pageable);
}
