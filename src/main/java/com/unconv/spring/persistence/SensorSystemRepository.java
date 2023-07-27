package com.unconv.spring.persistence;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorSystemRepository extends JpaRepository<SensorSystem, UUID> {
    @Query(
            "SELECT DISTINCT s.sensorLocation FROM SensorSystem s WHERE s.unconvUser.id = :unconvUserId")
    List<SensorLocation> findDistinctByUnconvUserId(@Param("unconvUserId") UUID unconvUserId);

    Page<SensorSystem> findAllByUnconvUserId(UUID unconvUserId, Pageable pageable);

    Page<SensorSystem> findByUnconvUserIdAndDeletedFalse(UUID unconvUserId, Pageable pageable);
}
