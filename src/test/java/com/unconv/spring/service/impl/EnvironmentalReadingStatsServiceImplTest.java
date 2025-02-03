package com.unconv.spring.service.impl;

import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.generateMockDataForDailyStats;
import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.generateMockDataForHourlyStats;
import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.generateMockDataForQuarterHourlyStats;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnvironmentalReadingStatsServiceImplTest {

    @Mock private EnvironmentalReadingRepository environmentalReadingRepository;

    @InjectMocks private EnvironmentalReadingStatsServiceImpl environmentalReadingStatsService;

    SensorSystem sensorSystem;
    UUID sensorSystemId;

    @BeforeEach
    void setUp() {
        sensorSystem = new SensorSystem();
        sensorSystemId = UUID.randomUUID();
        sensorSystem.setId(sensorSystemId);
    }

    @Test
    void getAverageTempsForQuarterHourly() {

        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForQuarterHourlyStats(sensorSystem, 5);

        when(environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        any(UUID.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(environmentalReadings);
        Map<OffsetDateTime, Double> result =
                environmentalReadingStatsService.getAverageTempsForQuarterHourly(sensorSystemId);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAverageTempsForHourly() {
        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForHourlyStats(sensorSystem, 5);

        when(environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        any(UUID.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(environmentalReadings);
        Map<OffsetDateTime, Double> result =
                environmentalReadingStatsService.getAverageTempsForHourly(sensorSystemId);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAverageTempsForDaily() {
        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForDailyStats(sensorSystem, 5);

        when(environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        any(UUID.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(environmentalReadings);
        Map<OffsetDateTime, Double> result =
                environmentalReadingStatsService.getAverageTempsForDaily(sensorSystemId);
        assertFalse(result.isEmpty());
    }
}
