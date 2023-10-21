package com.unconv.spring.web.rest;

import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.dto.TemperatureThresholdDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.TemperatureThresholdService;
import com.unconv.spring.utils.AppConstants;
import java.util.UUID;
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
@RequestMapping("/TemperatureThreshold")
@Slf4j
public class TemperatureThresholdController {

    @Autowired private TemperatureThresholdService temperatureThresholdService;

    @Autowired private ModelMapper modelMapper;

    @Autowired
    public TemperatureThresholdController(TemperatureThresholdService temperatureThresholdService) {
        this.temperatureThresholdService = temperatureThresholdService;
    }

    @GetMapping
    public PagedResult<TemperatureThreshold> getAllTemperatureThreshold(
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
        return temperatureThresholdService.findAllTemperatureThresholds(
                pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemperatureThreshold> getTemperatureThresholdById(@PathVariable UUID id) {
        return temperatureThresholdService
                .findTemperatureThresholdById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemperatureThreshold createTemperatureThreshold(
            @RequestBody @Validated TemperatureThresholdDTO temperatureThresholdDTO) {
        temperatureThresholdDTO.setId(null);
        return temperatureThresholdService.saveTemperatureThreshold(
                modelMapper.map(temperatureThresholdDTO, TemperatureThreshold.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemperatureThreshold> updateTemperatureThreshold(
            @PathVariable UUID id, @RequestBody TemperatureThresholdDTO temperatureThresholdDTO) {
        return temperatureThresholdService
                .findTemperatureThresholdById(id)
                .map(
                        temperatureThresholdObj -> {
                            temperatureThresholdDTO.setId(id);
                            return ResponseEntity.ok(
                                    temperatureThresholdService.saveTemperatureThreshold(
                                            modelMapper.map(
                                                    temperatureThresholdDTO,
                                                    TemperatureThreshold.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TemperatureThreshold> deleteTemperatureThreshold(@PathVariable UUID id) {
        return temperatureThresholdService
                .findTemperatureThresholdById(id)
                .map(
                        temperatureThreshold -> {
                            temperatureThresholdService.deleteTemperatureThresholdById(id);
                            return ResponseEntity.ok(temperatureThreshold);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
