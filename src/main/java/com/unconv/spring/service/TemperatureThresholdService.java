package com.unconv.spring.service;

import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

/** Service interface for managing {@link TemperatureThreshold}s. */
public interface TemperatureThresholdService {

    /**
     * Retrieves a paginated list of all temperature thresholds.
     *
     * @param pageNo the page number
     * @param pageSize the size of each page
     * @param sortBy the field to sort by
     * @param sortDir the direction of sorting
     * @return a {@link PagedResult} containing the temperature thresholds
     */
    PagedResult<TemperatureThreshold> findAllTemperatureThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves the temperature threshold with the specified ID.
     *
     * @param id the ID of the temperature threshold to retrieve
     * @return an {@link Optional} containing the temperature threshold, or empty if not found
     */
    Optional<TemperatureThreshold> findTemperatureThresholdById(UUID id);

    /**
     * Saves a temperature threshold.
     *
     * @param temperatureThreshold the temperature threshold to save
     * @return the saved temperature threshold
     */
    TemperatureThreshold saveTemperatureThreshold(TemperatureThreshold temperatureThreshold);

    /**
     * Deletes the temperature threshold with the specified ID.
     *
     * @param id the ID of the temperature threshold to delete
     */
    void deleteTemperatureThresholdById(UUID id);
}
