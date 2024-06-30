package com.unconv.spring.service.impl;

import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.HumidityThresholdRepository;
import com.unconv.spring.service.HumidityThresholdService;
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
public class HumidityThresholdServiceImpl implements HumidityThresholdService {

    @Autowired private HumidityThresholdRepository humidityThresholdRepository;

    /**
     * Retrieves a paginated list of all humidity thresholds.
     *
     * @param pageNo the page number
     * @param pageSize the size of each page
     * @param sortBy the field to sort by
     * @param sortDir the direction of sorting
     * @return a {@link PagedResult} containing the humidity thresholds
     */
    @Override
    public PagedResult<HumidityThreshold> findAllHumidityThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<HumidityThreshold> humidityThresholdPage =
                humidityThresholdRepository.findAll(pageable);

        return new PagedResult<>(humidityThresholdPage);
    }

    /**
     * Retrieves the humidity threshold with the specified ID.
     *
     * @param id the ID of the humidity threshold to retrieve
     * @return an {@link Optional} containing the humidity threshold, or empty if not found
     */
    @Override
    public Optional<HumidityThreshold> findHumidityThresholdById(UUID id) {
        return humidityThresholdRepository.findById(id);
    }

    /**
     * Saves a humidity threshold.
     *
     * @param humidityThreshold the humidity threshold to save
     * @return the saved humidity threshold
     */
    @Override
    public HumidityThreshold saveHumidityThreshold(HumidityThreshold humidityThreshold) {
        return humidityThresholdRepository.save(humidityThreshold);
    }

    /**
     * Deletes the humidity threshold with the specified ID.
     *
     * @param id the ID of the humidity threshold to delete
     */
    @Override
    public void deleteHumidityThresholdById(UUID id) {
        humidityThresholdRepository.deleteById(id);
    }
}
