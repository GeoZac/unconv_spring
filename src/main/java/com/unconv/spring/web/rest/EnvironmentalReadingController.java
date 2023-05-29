package com.unconv.spring.web.rest;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.utils.AppConstants;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/EnvironmentalReading")
@Slf4j
public class EnvironmentalReadingController {

    @Autowired private EnvironmentalReadingService environmentalReadingService;

    @Autowired private ModelMapper modelMapper;

    @GetMapping
    public PagedResult<EnvironmentalReading> getAllEnvironmentalReadings(
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return environmentalReadingService.findAllEnvironmentalReadings(
                pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("SensorSystem/{sensorSystemId}")
    public PagedResult<EnvironmentalReading> getAllEnvironmentalReadingsBySensorSystemId(
            @PathVariable UUID sensorSystemId,
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return environmentalReadingService.findAllEnvironmentalReadingsBySensorSystemId(
                sensorSystemId, pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentalReading> getEnvironmentalReadingById(@PathVariable UUID id) {
        return environmentalReadingService
                .findEnvironmentalReadingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnvironmentalReading createEnvironmentalReading(
            @RequestBody @Validated EnvironmentalReadingDTO environmentalReadingDTO) {
        return environmentalReadingService.saveEnvironmentalReading(
                modelMapper.map(environmentalReadingDTO, EnvironmentalReading.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnvironmentalReading> updateEnvironmentalReading(
            @PathVariable UUID id,
            @RequestBody @Valid EnvironmentalReadingDTO environmentalReadingDTO) {
        return environmentalReadingService
                .findEnvironmentalReadingById(id)
                .map(
                        environmentalReadingObj -> {
                            environmentalReadingDTO.setId(id);
                            return ResponseEntity.ok(
                                    environmentalReadingService.saveEnvironmentalReading(
                                            modelMapper.map(
                                                    environmentalReadingDTO,
                                                    EnvironmentalReading.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EnvironmentalReading> deleteEnvironmentalReading(@PathVariable UUID id) {
        return environmentalReadingService
                .findEnvironmentalReadingById(id)
                .map(
                        environmentalReading -> {
                            environmentalReadingService.deleteEnvironmentalReadingById(id);
                            return ResponseEntity.ok(environmentalReading);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/QuarterHourly/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getQuarterHourlyTemperature(
            @PathVariable UUID sensorSystemId) {
        Map<OffsetDateTime, Double> tenMinuteTemperatures =
                environmentalReadingService.getAverageTempsForQuarterHourly(sensorSystemId);
        return ResponseEntity.ok(tenMinuteTemperatures);
    }

    @GetMapping("/Hourly/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getHourlyTemperature(
            @PathVariable UUID sensorSystemId) {
        Map<OffsetDateTime, Double> hourlyTemperatures =
                environmentalReadingService.getAverageTempsForHourly(sensorSystemId);
        return ResponseEntity.ok(hourlyTemperatures);
    }

    @GetMapping("/Daily/SensorSystem/{sensorSystemId}")
    public ResponseEntity<Map<OffsetDateTime, Double>> getDailyTemperature(
            @PathVariable UUID sensorSystemId) {
        Map<OffsetDateTime, Double> hourlyTemperatures =
                environmentalReadingService.getAverageTempsForDaily(sensorSystemId);
        return ResponseEntity.ok(hourlyTemperatures);
    }
}
