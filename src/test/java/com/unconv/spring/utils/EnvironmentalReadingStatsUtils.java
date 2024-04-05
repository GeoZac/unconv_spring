package com.unconv.spring.utils;

import static org.instancio.Select.field;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.service.EnvironmentalReadingStatsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.instancio.Instancio;
import org.instancio.Model;

public class EnvironmentalReadingStatsUtils {
    public static List<EnvironmentalReading> generateMockDataForStats(
            SensorSystem sensorSystem, int listLength, TemporalAmount timePeriod) {
        List<EnvironmentalReading> environmentalReadings = new ArrayList<>();
        for (int i = 0; i < listLength; i++) {
            EnvironmentalReading environmentalReading =
                    Instancio.of(environemntalReadingModel)
                            .supply(
                                    field(EnvironmentalReading::getSensorSystem),
                                    () -> sensorSystem)
                            .supply(
                                    field(EnvironmentalReading::getTimestamp),
                                    random ->
                                            ZonedDateTime.of(
                                                            LocalDateTime.now()
                                                                    .minusMinutes(
                                                                            getTotalMinutes(
                                                                                    timePeriod))
                                                                    .plusMinutes(
                                                                            random.longRange(
                                                                                    0,
                                                                                    getTotalMinutes(
                                                                                            timePeriod))),
                                                            ZoneId.systemDefault())
                                                    .toOffsetDateTime())
                            .create();
            environmentalReadings.add(environmentalReading);
        }
        return environmentalReadings;
    }

    private static long getTotalMinutes(TemporalAmount timePeriod) {
        return Duration.from(timePeriod).toMinutes();
    }

    private static final Model<EnvironmentalReading> environemntalReadingModel =
            Instancio.of(EnvironmentalReading.class)
                    .supply(
                            field(EnvironmentalReading::getTemperature),
                            random ->
                                    BigDecimal.valueOf(random.doubleRange(-9999.000, 9999.000))
                                            .setScale(3, RoundingMode.HALF_UP)
                                            .doubleValue())
                    .supply(
                            field(EnvironmentalReading::getHumidity),
                            random ->
                                    BigDecimal.valueOf(random.doubleRange(0, 100))
                                            .setScale(3, RoundingMode.HALF_UP)
                                            .doubleValue())
                    .generate(
                            field(EnvironmentalReading::getTimestamp),
                            gen -> gen.temporal().offsetDateTime().past())
                    .ignore(field(EnvironmentalReading::getId))
                    .toModel();

    public static List<EnvironmentalReading> generateMockDataForQuarterHourlyStats(
            SensorSystem sensorSystem, int listLength) {
        return generateMockDataForStats(sensorSystem, listLength, Duration.ofHours(3));
    }

    public static Map<OffsetDateTime, Double> calculateAverageTempsForQuarterHourly(
            EnvironmentalReadingStatsService environmentalReadingStatsService,
            SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForQuarterHourlyStats(sensorSystem, 75);

        return environmentalReadingStatsService.getAverageTempsForHourly(environmentalReadings);
    }

    public static List<EnvironmentalReading> generateMockDataForHourlyStats(
            SensorSystem sensorSystem, int listLength) {
        return generateMockDataForStats(sensorSystem, listLength, Duration.ofHours(24));
    }

    public static Map<OffsetDateTime, Double> calculateAverageTempsForHourly(
            EnvironmentalReadingStatsService environmentalReadingStatsService,
            SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForHourlyStats(sensorSystem, 75);

        return environmentalReadingStatsService.getAverageTempsForHourly(environmentalReadings);
    }

    public static List<EnvironmentalReading> generateMockDataForDailyStats(
            SensorSystem sensorSystem, int listLength) {
        return generateMockDataForStats(sensorSystem, listLength, Duration.ofDays(7));
    }

    public static Map<OffsetDateTime, Double> calculateAverageTempsForDaily(
            EnvironmentalReadingStatsService environmentalReadingStatsService,
            SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForDailyStats(sensorSystem, 150);
        return environmentalReadingStatsService.getAverageTempsForHourly(environmentalReadings);
    }
}
