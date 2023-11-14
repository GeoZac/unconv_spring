package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.dto.SensorLocationDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.SensorLocationService;
import java.util.List;
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
@RequestMapping("/SensorLocation")
@Slf4j
public class SensorLocationController {

    @Autowired private SensorLocationService sensorLocationService;

    @Autowired private ModelMapper modelMapper;

    @GetMapping
    public PagedResult<SensorLocation> getAllSensorLocations(
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
        return sensorLocationService.findAllSensorLocations(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/UnconvUser/{unconvUserId}")
    public List<SensorLocation> getAllSensorSystemsByUnconvUserId(@PathVariable UUID unconvUserId) {
        return sensorLocationService.findAllSensorLocationsByUnconvUserId(unconvUserId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorLocation> getSensorLocationById(@PathVariable UUID id) {
        return sensorLocationService
                .findSensorLocationById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorLocation createSensorLocation(
            @RequestBody @Validated SensorLocationDTO sensorLocationDTO) {
        sensorLocationDTO.setId(null);
        return sensorLocationService.saveSensorLocation(
                modelMapper.map(sensorLocationDTO, SensorLocation.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorLocation> updateSensorLocation(
            @PathVariable UUID id, @RequestBody @Valid SensorLocationDTO sensorLocationDTO) {
        return sensorLocationService
                .findSensorLocationById(id)
                .map(
                        sensorLocationObj -> {
                            sensorLocationDTO.setId(id);
                            return ResponseEntity.ok(
                                    sensorLocationService.saveSensorLocation(
                                            modelMapper.map(
                                                    sensorLocationDTO, SensorLocation.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SensorLocation> deleteSensorLocation(@PathVariable UUID id) {
        return sensorLocationService
                .findSensorLocationById(id)
                .map(
                        sensorLocation -> {
                            sensorLocationService.deleteSensorLocationById(id);
                            return ResponseEntity.ok(sensorLocation);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
