package com.unconv.spring.service;

import com.unconv.spring.domain.EnvironmentalReading;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EnvironmentalReadingStatsService {

    Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(UUID sensorSystemId);

    Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(List<EnvironmentalReading> data);

    Map<OffsetDateTime, Double> getAverageTempsForHourly(UUID sensorSystemId);

    Map<OffsetDateTime, Double> getAverageTempsForHourly(List<EnvironmentalReading> data);

    Map<OffsetDateTime, Double> getAverageTempsForDaily(UUID sensorSystemId);

    Map<OffsetDateTime, Double> getAverageTempsForDaily(List<EnvironmentalReading> data);
}
