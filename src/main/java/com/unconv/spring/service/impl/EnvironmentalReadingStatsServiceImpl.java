package com.unconv.spring.service.impl;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.service.EnvironmentalReadingStatsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnvironmentalReadingStatsServiceImpl implements EnvironmentalReadingStatsService {

    private final EnvironmentalReadingRepository environmentalReadingRepository;

    /**
     * Constructs an instance of {@link EnvironmentalReadingStatsServiceImpl} with the specified
     * repository.
     *
     * @param environmentalReadingRepository the repository for accessing environmental reading data
     */
    public EnvironmentalReadingStatsServiceImpl(
            EnvironmentalReadingRepository environmentalReadingRepository) {
        this.environmentalReadingRepository = environmentalReadingRepository;
    }

    /**
     * Calculates the average temperatures for each quarter-hourly interval for a specified sensor
     * system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a map containing the average temperatures for each quarter-hourly interval
     */
    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(3),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForQuarterHourly(data));
    }

    /**
     * Calculates the average temperatures for each quarter-hourly interval based on the provided
     * environmental readings.
     *
     * @param data a list of environmental readings
     * @return a map containing the average temperatures for each quarter-hourly interval
     */
    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(
            List<EnvironmentalReading> data) {
        OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startTime = endTime.minusHours(3);
        Duration interval = Duration.ofMinutes(15);

        Map<OffsetDateTime, List<EnvironmentalReading>> groupedData =
                data.stream()
                        .filter(d -> d.getTimestamp().isAfter(startTime))
                        .collect(
                                Collectors.groupingBy(
                                        d -> roundTimeToInterval(d.getTimestamp(), interval)));

        return groupedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey, e -> calculateAverageTemp(e.getValue())));
    }

    /**
     * Calculates the average temperatures for each hourly interval for a specified sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a map containing the average temperatures for each hourly interval
     */
    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForHourly(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(24),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForHourly(data));
    }

    /**
     * Calculates the average temperatures for each hourly interval based on the provided
     * environmental readings.
     *
     * @param data a list of environmental readings
     * @return a map containing the average temperatures for each hourly interval
     */
    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForHourly(List<EnvironmentalReading> data) {
        OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startTime = endTime.minusHours(24);
        Duration interval = Duration.ofMinutes(60);

        Map<OffsetDateTime, List<EnvironmentalReading>> groupedData =
                data.stream()
                        .filter(d -> d.getTimestamp().isAfter(startTime))
                        .collect(
                                Collectors.groupingBy(
                                        d -> roundTimeToInterval(d.getTimestamp(), interval)));

        return groupedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey, e -> calculateAverageTemp(e.getValue())));
    }

    /**
     * Calculates the average temperatures for each daily interval for a specified sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a map containing the average temperatures for each daily interval
     */
    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForDaily(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusDays(7),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForDaily(data));
    }

    /**
     * Calculates the average temperatures for each daily interval based on the provided
     * environmental readings.
     *
     * @param data a list of environmental readings
     * @return a map containing the average temperatures for each daily interval
     */
    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForDaily(List<EnvironmentalReading> data) {
        OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startTime = endTime.minusDays(7);
        Duration interval = Duration.ofDays(1);

        Map<OffsetDateTime, List<EnvironmentalReading>> groupedData =
                data.stream()
                        .filter(d -> d.getTimestamp().isAfter(startTime))
                        .collect(
                                Collectors.groupingBy(
                                        d -> roundTimeToInterval(d.getTimestamp(), interval)));

        return groupedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey, e -> calculateAverageTemp(e.getValue())));
    }

    private OffsetDateTime roundTimeToInterval(OffsetDateTime dateTime, Duration interval) {
        long seconds = dateTime.toEpochSecond() / interval.getSeconds() * interval.getSeconds();
        Instant instant = Instant.ofEpochSecond(seconds);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private double calculateAverageTemp(List<EnvironmentalReading> data) {
        double sum = data.stream().mapToDouble(EnvironmentalReading::getTemperature).sum();
        return BigDecimal.valueOf(sum / data.size())
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
