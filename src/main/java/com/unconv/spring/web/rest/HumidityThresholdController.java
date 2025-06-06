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

/**
 * Controller class responsible for handling HTTP requests related to {@link HumidityThreshold}. It
 * provides endpoints for managing humidity thresholds
 */
@RestController
@RequestMapping("/HumidityThreshold")
@Slf4j
public class HumidityThresholdController {

    @Autowired private HumidityThresholdService humidityThresholdService;

    @Autowired private ModelMapper modelMapper;

    /**
     * Constructs a {@link HumidityThresholdController} with the specified {@link
     * HumidityThresholdService}.
     *
     * @param humidityThresholdService the service to manage humidity thresholds
     */
    public HumidityThresholdController(HumidityThresholdService humidityThresholdService) {
        this.humidityThresholdService = humidityThresholdService;
    }

    /**
     * Retrieves a paginated list of humidity thresholds.
     *
     * @param pageNo the page number to retrieve (default is {@link
     *     AppConstants#DEFAULT_PAGE_NUMBER})
     * @param pageSize the number of items per page (default is {@link
     *     AppConstants#DEFAULT_PAGE_SIZE})
     * @param sortBy the field to sort by (default is {@link AppConstants#DEFAULT_SORT_BY})
     * @param sortDir the direction to sort (default is {@link AppConstants#DEFAULT_SORT_DIRECTION})
     * @return a {@link PagedResult} containing a list of {@link HumidityThreshold}
     */
    @GetMapping
    public PagedResult<HumidityThreshold> getAllHumidityThreshold(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        return humidityThresholdService.findAllHumidityThresholds(
                pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Retrieves a humidity threshold by its ID.
     *
     * @param id the UUID of the humidity threshold to retrieve
     * @return a {@link ResponseEntity} containing the {@link HumidityThreshold} if found, or {@link
     *     ResponseEntity#notFound()} if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<HumidityThreshold> getHumidityThresholdById(@PathVariable UUID id) {
        return humidityThresholdService
                .findHumidityThresholdById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new humidity threshold.
     *
     * @param humidityThresholdDTO the DTO containing the details of the humidity threshold to
     *     create
     * @return the created {@link HumidityThreshold}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HumidityThreshold createHumidityThreshold(
            @RequestBody @Validated HumidityThresholdDTO humidityThresholdDTO) {
        humidityThresholdDTO.setId(null);
        return humidityThresholdService.saveHumidityThreshold(
                modelMapper.map(humidityThresholdDTO, HumidityThreshold.class));
    }

    /**
     * Updates an existing humidity threshold.
     *
     * @param id the UUID of the humidity threshold to update
     * @param humidityThresholdDTO the DTO containing the updated details of the humidity threshold
     * @return a {@link ResponseEntity} containing the updated {@link HumidityThreshold} if found,
     *     or {@link ResponseEntity#notFound()} if not found
     */
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

    /**
     * Deletes a humidity threshold by its ID.
     *
     * @param id the UUID of the humidity threshold to delete
     * @return a {@link ResponseEntity} containing the deleted {@link HumidityThreshold} if found,
     *     or {@link ResponseEntity#notFound()} if not found
     */
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
