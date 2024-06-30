package com.unconv.spring.service.impl;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorLocationRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.service.SensorLocationService;
import java.util.List;
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
public class SensorLocationServiceImpl implements SensorLocationService {

    @Autowired private SensorLocationRepository sensorLocationRepository;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    /**
     * Retrieves a paginated list of all {@link SensorLocation}s.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of SensorLocations.
     */
    @Override
    public PagedResult<SensorLocation> findAllSensorLocations(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<SensorLocation> sensorLocationsPage = sensorLocationRepository.findAll(pageable);

        return new PagedResult<>(sensorLocationsPage);
    }

    /**
     * Retrieves a list of {@link SensorLocation}s by UnconvUserId.
     *
     * @param unconvUserId The ID of the {@link UnconvUser}.
     * @return A list of {@link SensorLocation}s associated with the {@link UnconvUser}.
     */
    @Override
    public List<SensorLocation> findAllSensorLocationsByUnconvUserId(UUID unconvUserId) {
        return sensorSystemRepository.findDistinctByUnconvUserId(unconvUserId);
    }

    /**
     * Retrieves a {@link SensorLocation} by its ID.
     *
     * @param id The ID of the {@link SensorLocation}.
     * @return An {@link Optional} containing the {@link SensorLocation}, or empty if not found.
     */
    @Override
    public Optional<SensorLocation> findSensorLocationById(UUID id) {
        return sensorLocationRepository.findById(id);
    }

    /**
     * Saves a new {@link SensorLocation}.
     *
     * @param sensorLocation The {@link SensorLocation} to save.
     * @return The saved {@link SensorLocation}.
     */
    @Override
    public SensorLocation saveSensorLocation(SensorLocation sensorLocation) {
        return sensorLocationRepository.save(sensorLocation);
    }

    /**
     * Deletes a {@link SensorLocation} by its ID.
     *
     * @param id The ID of the {@link SensorLocation} to delete.
     */
    @Override
    public void deleteSensorLocationById(UUID id) {
        sensorLocationRepository.deleteById(id);
    }
}
