package com.unconv.spring.web.rest;

import com.unconv.spring.service.EnvironmentalReadingStatsService;
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

@RestController
@RequestMapping("/EnvironmentalReadingStats")
@Slf4j
public class EnvironmentalReadingStatsController {

    private final EnvironmentalReadingStatsService environmentalReadingStatsService;

    @Autowired
    public EnvironmentalReadingStatsController(
            EnvironmentalReadingStatsService environmentalReadingStatsService) {
        this.environmentalReadingStatsService = environmentalReadingStatsService;
    }

    @GetMapping("/QuarterHourly/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getQuarterHourlyTemperature(
            @PathVariable UUID sensorSystemId) {
        Map<OffsetDateTime, Double> tenMinuteTemperatures =
                environmentalReadingStatsService.getAverageTempsForQuarterHourly(sensorSystemId);
        return ResponseEntity.ok(tenMinuteTemperatures);
    }

    @GetMapping("/Hourly/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getHourlyTemperature(
            @PathVariable UUID sensorSystemId) {
        Map<OffsetDateTime, Double> hourlyTemperatures =
                environmentalReadingStatsService.getAverageTempsForHourly(sensorSystemId);
        return ResponseEntity.ok(hourlyTemperatures);
    }

    @GetMapping("/Daily/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getDailyTemperature(
            @PathVariable UUID sensorSystemId) {
        Map<OffsetDateTime, Double> hourlyTemperatures =
                environmentalReadingStatsService.getAverageTempsForDaily(sensorSystemId);
        return ResponseEntity.ok(hourlyTemperatures);
    }
}
