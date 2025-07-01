package com.unconv.spring.service;

import com.unconv.spring.domain.EnvironmentalReading;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EnvironmentalReadingStatsService defines methods for calculating average temperatures based on
 * environmental readings.
 */
public interface EnvironmentalReadingStatsService {

    /**
     * Calculates the average temperatures for each quarter-hourly interval for a specified sensor
     * system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a map containing the average temperatures for each quarter-hourly interval
     */
    Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(UUID sensorSystemId);

    /**
     * Calculates the average temperatures for each quarter-hourly interval based on the provided
     * environmental readings.
     *
     * @param data a list of environmental readings
     * @return a map containing the average temperatures for each quarter-hourly interval
     */
    Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(List<EnvironmentalReading> data);

    /**
     * Calculates the average temperatures for each hourly interval for a specified sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a map containing the average temperatures for each hourly interval
     */
    Map<OffsetDateTime, Double> getAverageTempsForHourly(UUID sensorSystemId);

    /**
     * Calculates the average temperatures for each hourly interval based on the provided
     * environmental readings.
     *
     * @param data a list of environmental readings
     * @return a map containing the average temperatures for each hourly interval
     */
    Map<OffsetDateTime, Double> getAverageTempsForHourly(List<EnvironmentalReading> data);

    /**
     * Calculates the average temperatures for each daily interval for a specified sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a map containing the average temperatures for each daily interval
     */
    Map<OffsetDateTime, Double> getAverageTempsForDaily(UUID sensorSystemId);

    /**
     * Calculates the average temperatures for each daily interval based on the provided
     * environmental readings.
     *
     * @param data a list of environmental readings
     * @return a map containing the average temperatures for each daily interval
     */
    Map<OffsetDateTime, Double> getAverageTempsForDaily(List<EnvironmentalReading> data);
}
