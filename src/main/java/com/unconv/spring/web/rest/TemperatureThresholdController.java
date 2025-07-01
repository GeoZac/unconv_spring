package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.dto.TemperatureThresholdDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.TemperatureThresholdService;
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

/**
 * Controller class responsible for handling HTTP requests related to {@link TemperatureThreshold}.
 * It provides endpoints for managing temperature thresholds
 */
@RestController
@RequestMapping("/TemperatureThreshold")
@Slf4j
public class TemperatureThresholdController {

    @Autowired private TemperatureThresholdService temperatureThresholdService;

    @Autowired private ModelMapper modelMapper;

    /**
     * Constructs a {@link TemperatureThresholdController} with the specified {@link
     * TemperatureThresholdService}.
     *
     * @param temperatureThresholdService the service to manage temperature thresholds
     */
    public TemperatureThresholdController(TemperatureThresholdService temperatureThresholdService) {
        this.temperatureThresholdService = temperatureThresholdService;
    }

    /**
     * Retrieves a paginated list of temperature thresholds.
     *
     * @param pageNo the page number to retrieve (default is {@link
     *     AppConstants#DEFAULT_PAGE_NUMBER})
     * @param pageSize the number of items per page (default is {@link
     *     AppConstants#DEFAULT_PAGE_SIZE})
     * @param sortBy the field to sort by (default is {@link AppConstants#DEFAULT_SORT_BY})
     * @param sortDir the direction to sort (default is {@link AppConstants#DEFAULT_SORT_DIRECTION})
     * @return a {@link PagedResult} containing a list of {@link TemperatureThreshold}
     */
    @GetMapping
    public PagedResult<TemperatureThreshold> getAllTemperatureThreshold(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        return temperatureThresholdService.findAllTemperatureThresholds(
                pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Retrieves a temperature threshold by its ID.
     *
     * @param id the UUID of the temperature threshold to retrieve
     * @return a {@link ResponseEntity} containing the {@link TemperatureThreshold} if found, or
     *     {@link ResponseEntity#notFound()} if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemperatureThreshold> getTemperatureThresholdById(@PathVariable UUID id) {
        return temperatureThresholdService
                .findTemperatureThresholdById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new temperature threshold.
     *
     * @param temperatureThresholdDTO the DTO containing the details of the temperature threshold to
     *     create
     * @return the created {@link TemperatureThreshold}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemperatureThreshold createTemperatureThreshold(
            @RequestBody @Validated TemperatureThresholdDTO temperatureThresholdDTO) {
        temperatureThresholdDTO.setId(null);
        return temperatureThresholdService.saveTemperatureThreshold(
                modelMapper.map(temperatureThresholdDTO, TemperatureThreshold.class));
    }

    /**
     * Updates an existing temperature threshold.
     *
     * @param id the UUID of the temperature threshold to update
     * @param temperatureThresholdDTO the DTO containing the updated details of the temperature
     *     threshold
     * @return a {@link ResponseEntity} containing the updated {@link TemperatureThreshold} if
     *     found, or {@link ResponseEntity#notFound()} if not found
     */
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

    /**
     * Deletes a temperature threshold by its ID.
     *
     * @param id the UUID of the temperature threshold to delete
     * @return a {@link ResponseEntity} containing the deleted {@link TemperatureThreshold} if
     *     found, or {@link ResponseEntity#notFound()} if not found
     */
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
