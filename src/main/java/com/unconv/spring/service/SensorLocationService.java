package com.unconv.spring.service;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.model.response.PagedResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Service interface for managing {@link SensorLocation}s. */
public interface SensorLocationService {

    /**
     * Retrieves a paginated list of all {@link SensorLocation}s.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of SensorLocations.
     */
    PagedResult<SensorLocation> findAllSensorLocations(
            int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves a list of {@link SensorLocation}s by UnconvUserId.
     *
     * @param unconvUserId The ID of the {@link UnconvUser}.
     * @return A list of {@link SensorLocation}s associated with the {@link UnconvUser}.
     */
    List<SensorLocation> findAllSensorLocationsByUnconvUserId(UUID unconvUserId);

    /**
     * Retrieves a {@link SensorLocation} by its ID.
     *
     * @param id The ID of the {@link SensorLocation}.
     * @return An {@link Optional} containing the {@link SensorLocation}, or empty if not found.
     */
    Optional<SensorLocation> findSensorLocationById(UUID id);

    /**
     * Saves a new {@link SensorLocation}.
     *
     * @param sensorLocation The {@link SensorLocation} to save.
     * @return The saved {@link SensorLocation}.
     */
    SensorLocation saveSensorLocation(SensorLocation sensorLocation);

    /**
     * Deletes a {@link SensorLocation} by its ID.
     *
     * @param id The ID of the {@link SensorLocation} to delete.
     */
    void deleteSensorLocationById(UUID id);
}
