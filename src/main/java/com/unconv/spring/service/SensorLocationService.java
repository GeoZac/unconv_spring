package com.unconv.spring.service;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.model.response.PagedResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Service interface for managing SensorLocations. */
public interface SensorLocationService {

    /**
     * Retrieves a paginated list of all SensorLocations.
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
     * Retrieves a list of SensorLocations by UnconvUserId.
     *
     * @param unconvUserId The ID of the UnconvUser.
     * @return A list of SensorLocations associated with the UnconvUser.
     */
    List<SensorLocation> findAllSensorLocationsByUnconvUserId(UUID unconvUserId);

    /**
     * Retrieves a SensorLocation by its ID.
     *
     * @param id The ID of the SensorLocation.
     * @return An Optional containing the SensorLocation, or empty if not found.
     */
    Optional<SensorLocation> findSensorLocationById(UUID id);

    /**
     * Saves a new SensorLocation.
     *
     * @param sensorLocation The SensorLocation to save.
     * @return The saved SensorLocation.
     */
    SensorLocation saveSensorLocation(SensorLocation sensorLocation);

    /**
     * Deletes a SensorLocation by its ID.
     *
     * @param id The ID of the SensorLocation to delete.
     */
    void deleteSensorLocationById(UUID id);
}
