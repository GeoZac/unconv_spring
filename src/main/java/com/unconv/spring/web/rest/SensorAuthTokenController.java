package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.dto.SensorAuthTokenDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.SensorAuthTokenService;
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
@RequestMapping("/SensorAuthToken")
@Slf4j
public class SensorAuthTokenController {

    private final SensorAuthTokenService sensorAuthTokenService;

    private final ModelMapper modelMapper;

    @Autowired
    public SensorAuthTokenController(
            SensorAuthTokenService sensorAuthTokenService, ModelMapper modelMapper) {
        this.sensorAuthTokenService = sensorAuthTokenService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public PagedResult<SensorAuthToken> getAllSensorAuthTokens(
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
        return sensorAuthTokenService.findAllSensorAuthTokens(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorAuthToken> getSensorAuthTokenById(@PathVariable UUID id) {
        return sensorAuthTokenService
                .findSensorAuthTokenById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorAuthToken createSensorAuthToken(
            @RequestBody @Validated SensorAuthTokenDTO sensorAuthTokenDTO) {
        sensorAuthTokenDTO.setId(null);
        return sensorAuthTokenService.saveSensorAuthToken(
                modelMapper.map(sensorAuthTokenDTO, SensorAuthToken.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorAuthToken> updateSensorAuthToken(
            @PathVariable UUID id, @RequestBody @Valid SensorAuthTokenDTO sensorAuthTokenDTO) {
        return sensorAuthTokenService
                .findSensorAuthTokenById(id)
                .map(
                        sensorAuthTokenObj -> {
                            sensorAuthTokenDTO.setId(id);
                            return ResponseEntity.ok(
                                    sensorAuthTokenService.saveSensorAuthToken(
                                            modelMapper.map(
                                                    sensorAuthTokenDTO, SensorAuthToken.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SensorAuthToken> deleteSensorAuthToken(@PathVariable UUID id) {
        return sensorAuthTokenService
                .findSensorAuthTokenById(id)
                .map(
                        sensorAuthToken -> {
                            sensorAuthTokenService.deleteSensorAuthTokenById(id);
                            return ResponseEntity.ok(sensorAuthToken);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
