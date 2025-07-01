package com.unconv.spring.service;

import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

/** Service interface for managing {@link HumidityThreshold}s. */
public interface HumidityThresholdService {

    /**
     * Retrieves a paginated list of all humidity thresholds.
     *
     * @param pageNo the page number
     * @param pageSize the size of each page
     * @param sortBy the field to sort by
     * @param sortDir the direction of sorting
     * @return a {@link PagedResult} containing the humidity thresholds
     */
    PagedResult<HumidityThreshold> findAllHumidityThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves the humidity threshold with the specified ID.
     *
     * @param id the ID of the humidity threshold to retrieve
     * @return an {@link Optional} containing the humidity threshold, or empty if not found
     */
    Optional<HumidityThreshold> findHumidityThresholdById(UUID id);

    /**
     * Saves a humidity threshold.
     *
     * @param humidityThreshold the humidity threshold to save
     * @return the saved humidity threshold
     */
    HumidityThreshold saveHumidityThreshold(HumidityThreshold humidityThreshold);

    /**
     * Deletes the humidity threshold with the specified ID.
     *
     * @param id the ID of the humidity threshold to delete
     */
    void deleteHumidityThresholdById(UUID id);
}
