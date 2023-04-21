package com.unconv.spring.persistence;

import com.unconv.spring.domain.EnvironmentalReading;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EnvironmentalReadingRepository extends JpaRepository<EnvironmentalReading, UUID> {

    @Query(
            value =
                    "SELECT DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:00') AS interval, temperature "
                            + "FROM environmental_reading "
                            + "WHERE timestamp BETWEEN DATE_SUB(NOW(), INTERVAL 2 HOUR) AND NOW() "
                            + "AND MINUTE(timestamp) DIV 10 = 0 "
                            + "ORDER BY timestamp DESC",
            nativeQuery = true)
    List<Object[]> findTemperatureBy10MinuteInterval();
}
