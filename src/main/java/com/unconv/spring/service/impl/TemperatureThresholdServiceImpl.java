package com.unconv.spring.service.impl;

import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.TemperatureThresholdRepository;
import com.unconv.spring.service.TemperatureThresholdService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TemperatureThresholdServiceImpl implements TemperatureThresholdService {

    @Autowired private TemperatureThresholdRepository temperatureThresholdRepository;

    /**
     * Retrieves a paginated list of all temperature thresholds.
     *
     * @param pageNo the page number
     * @param pageSize the size of each page
     * @param sortBy the field to sort by
     * @param sortDir the direction of sorting
     * @return a {@link PagedResult} containing the temperature thresholds
     */
    @Override
    public PagedResult<TemperatureThreshold> findAllTemperatureThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<TemperatureThreshold> temperatureThresholdPage =
                temperatureThresholdRepository.findAll(pageable);

        return new PagedResult<>(temperatureThresholdPage);
    }

    /**
     * Retrieves the temperature threshold with the specified ID.
     *
     * @param id the ID of the temperature threshold to retrieve
     * @return an {@link Optional} containing the temperature threshold, or empty if not found
     */
    @Override
    public Optional<TemperatureThreshold> findTemperatureThresholdById(UUID id) {
        return temperatureThresholdRepository.findById(id);
    }

    /**
     * Saves a temperature threshold.
     *
     * @param temperatureThreshold the temperature threshold to save
     * @return the saved temperature threshold
     */
    @Override
    public TemperatureThreshold saveTemperatureThreshold(
            TemperatureThreshold temperatureThreshold) {
        return temperatureThresholdRepository.save(temperatureThreshold);
    }

    /**
     * Deletes the temperature threshold with the specified ID.
     *
     * @param id the ID of the temperature threshold to delete
     */
    @Override
    public void deleteTemperatureThresholdById(UUID id) {
        temperatureThresholdRepository.deleteById(id);
    }
}
