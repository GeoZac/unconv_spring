package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.SensorLocationDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.SensorLocationService;
import com.unconv.spring.service.UnconvUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
 * Controller class responsible for handling HTTP requests related to {@link SensorLocation}. It
 * provides endpoints for managing sensor systems.
 */
@RestController
@RequestMapping("/SensorLocation")
@Slf4j
public class SensorLocationController {

    private final SensorLocationService sensorLocationService;

    private final UnconvUserService unconvUserService;

    private final ModelMapper modelMapper;

    /**
     * Constructs a {@link SensorLocationController} with the specified services and model mapper.
     *
     * @param sensorLocationService the service to manage sensor locations
     * @param unconvUserService the service to manage users
     * @param modelMapper the mapper to convert between DTOs and entities
     */
    public SensorLocationController(
            SensorLocationService sensorLocationService,
            UnconvUserService unconvUserService,
            ModelMapper modelMapper) {
        this.sensorLocationService = sensorLocationService;
        this.unconvUserService = unconvUserService;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves a paginated list of sensor locations.
     *
     * @param pageNo The page number to retrieve (default is 0).
     * @param pageSize The size of each page (default is 10).
     * @param sortBy The field to sort by (default is "sensorName").
     * @param sortDir The direction of sorting (default is "asc" for ascending).
     * @return A {@link PagedResult} containing the paginated list of {@link SensorLocation}s.
     */
    @GetMapping
    public PagedResult<SensorLocation> getAllSensorLocations(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        return sensorLocationService.findAllSensorLocations(pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Retrieves a paginated list of sensor systems associated with a specific {@link UnconvUser}.
     *
     * @param unconvUserId The UUID of the unconventional user.
     * @return A {@link PagedResult} containing the paginated list of {@link SensorLocation}s.
     */
    @GetMapping("/UnconvUser/{unconvUserId}")
    public ResponseEntity<List<SensorLocation>> getAllSensorSystemsByUnconvUserId(
            @PathVariable UUID unconvUserId) {
        return unconvUserService
                .findUnconvUserById(unconvUserId)
                .map(
                        obj -> {
                            List<SensorLocation> sensorLocations =
                                    sensorLocationService.findAllSensorLocationsByUnconvUserId(
                                            unconvUserId);
                            return ResponseEntity.ok(sensorLocations);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a SensorLocation by its ID.
     *
     * @param id The ID of the SensorLocation to retrieve.
     * @return ResponseEntity with status 200 (OK) and the retrieved SensorLocation if found, or
     *     ResponseEntity with status 404 (Not Found) if no SensorLocation with the given ID exists.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SensorLocation> getSensorLocationById(@PathVariable UUID id) {
        return sensorLocationService
                .findSensorLocationById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new SensorLocation based on the provided SensorLocationDTO.
     *
     * @param sensorLocationDTO The SensorLocationDTO containing the data for the new
     *     SensorLocation.
     * @return The created SensorLocation entity with HTTP status 201 (Created).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorLocation createSensorLocation(
            @RequestBody @Validated SensorLocationDTO sensorLocationDTO) {
        sensorLocationDTO.setId(null);
        return sensorLocationService.saveSensorLocation(
                modelMapper.map(sensorLocationDTO, SensorLocation.class));
    }

    /**
     * Updates an existing SensorLocation identified by the given ID with the data from the provided
     * SensorLocationDTO.
     *
     * @param id The ID of the SensorLocation to update.
     * @param sensorLocationDTO The updated data for the SensorLocation.
     * @return ResponseEntity with status 200 (OK) and the updated SensorLocation if found and
     *     updated successfully, or ResponseEntity with status 404 (Not Found) if no SensorLocation
     *     with the given ID exists.
     */
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

    /**
     * Deletes a SensorLocation identified by the given ID.
     *
     * @param id The ID of the SensorLocation to delete.
     * @return ResponseEntity with status 200 (OK) and the deleted SensorLocation if found and
     *     deleted successfully, or ResponseEntity with status 404 (Not Found) if no SensorLocation
     *     with the given ID exists.
     */
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
