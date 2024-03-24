package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.dto.HumidityThresholdDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.HumidityThresholdService;
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
@RequestMapping("/HumidityThreshold")
@Slf4j
public class HumidityThresholdController {

    @Autowired private HumidityThresholdService humidityThresholdService;

    @Autowired private ModelMapper modelMapper;

    @Autowired
    public HumidityThresholdController(HumidityThresholdService humidityThresholdService) {
        this.humidityThresholdService = humidityThresholdService;
    }

    @GetMapping
    public PagedResult<HumidityThreshold> getAllHumidityThreshold(
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
        return humidityThresholdService.findAllHumidityThresholds(
                pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HumidityThreshold> getHumidityThresholdById(@PathVariable UUID id) {
        return humidityThresholdService
                .findHumidityThresholdById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HumidityThreshold createHumidityThreshold(
            @RequestBody @Validated HumidityThresholdDTO humidityThresholdDTO) {
        humidityThresholdDTO.setId(null);
        return humidityThresholdService.saveHumidityThreshold(
                modelMapper.map(humidityThresholdDTO, HumidityThreshold.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HumidityThreshold> updateHumidityThreshold(
            @PathVariable UUID id, @RequestBody HumidityThresholdDTO humidityThresholdDTO) {
        return humidityThresholdService
                .findHumidityThresholdById(id)
                .map(
                        humidityThresholdObj -> {
                            humidityThresholdDTO.setId(id);
                            return ResponseEntity.ok(
                                    humidityThresholdService.saveHumidityThreshold(
                                            modelMapper.map(
                                                    humidityThresholdDTO,
                                                    HumidityThreshold.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HumidityThreshold> deleteHumidityThreshold(@PathVariable UUID id) {
        return humidityThresholdService
                .findHumidityThresholdById(id)
                .map(
                        humidityThreshold -> {
                            humidityThresholdService.deleteHumidityThresholdById(id);
                            return ResponseEntity.ok(humidityThreshold);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
