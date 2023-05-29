package com.unconv.spring.persistence;

import com.unconv.spring.domain.EnvironmentalReading;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentalReadingRepository extends JpaRepository<EnvironmentalReading, UUID> {

    List<EnvironmentalReading> findByTimestampBetween(OffsetDateTime start, OffsetDateTime end);

    List<EnvironmentalReading> findBySensorSystemIdAndTimestampBetween(
            UUID sensorSystemId, OffsetDateTime start, OffsetDateTime end);

    Page<EnvironmentalReading> findAllBySensorSystemId(UUID sensorSystemId, Pageable pageable);
}
