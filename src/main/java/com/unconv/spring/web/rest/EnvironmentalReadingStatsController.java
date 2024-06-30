package com.unconv.spring.web.rest;

import com.unconv.spring.service.EnvironmentalReadingStatsService;
import com.unconv.spring.service.SensorSystemService;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller class for handling environmental reading statistics requests. */
@RestController
@RequestMapping("/EnvironmentalReadingStats")
@Slf4j
public class EnvironmentalReadingStatsController {

    private final EnvironmentalReadingStatsService environmentalReadingStatsService;

    private final SensorSystemService sensorSystemService;

    /**
     * Constructs a new EnvironmentalReadingStatsController with the specified services.
     *
     * @param environmentalReadingStatsService the service for environmental reading statistics
     * @param sensorSystemService the service for sensor systems
     */
    @Autowired
    public EnvironmentalReadingStatsController(
            EnvironmentalReadingStatsService environmentalReadingStatsService,
            SensorSystemService sensorSystemService) {
        this.environmentalReadingStatsService = environmentalReadingStatsService;
        this.sensorSystemService = sensorSystemService;
    }

    /**
     * Retrieves the quarter-hourly temperature statistics for the specified sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a {@link ResponseEntity} containing a map of quarter-hourly temperatures
     */
    @GetMapping("/QuarterHourly/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getQuarterHourlyTemperature(
            @PathVariable UUID sensorSystemId) {
        return sensorSystemService
                .findSensorSystemById(sensorSystemId)
                .map(
                        sensorSystem -> {
                            Map<OffsetDateTime, Double> quarterHourlyTemperatures =
                                    environmentalReadingStatsService
                                            .getAverageTempsForQuarterHourly(sensorSystem.getId());
                            return ResponseEntity.ok(quarterHourlyTemperatures);
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves the hourly temperature statistics for the specified sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a {@link ResponseEntity} containing a map of hourly temperatures
     */
    @GetMapping("/Hourly/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getHourlyTemperature(
            @PathVariable UUID sensorSystemId) {
        return sensorSystemService
                .findSensorSystemById(sensorSystemId)
                .map(
                        sensorSystem -> {
                            Map<OffsetDateTime, Double> hourlyTemperatures =
                                    environmentalReadingStatsService.getAverageTempsForHourly(
                                            sensorSystem.getId());
                            return ResponseEntity.ok(hourlyTemperatures);
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves the daily temperature statistics for the specified sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a {@link ResponseEntity} containing a map of daily temperatures
     */
    @GetMapping("/Daily/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getDailyTemperature(
            @PathVariable UUID sensorSystemId) {

        return sensorSystemService
                .findSensorSystemById(sensorSystemId)
                .map(
                        sensorSystem -> {
                            Map<OffsetDateTime, Double> dailyTemperatures =
                                    environmentalReadingStatsService.getAverageTempsForDaily(
                                            sensorSystem.getId());
                            return ResponseEntity.ok(dailyTemperatures);
                        })
                .orElse(ResponseEntity.notFound().build());
    }
}
