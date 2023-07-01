package com.unconv.spring.service.impl;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorLocationRepository;
import com.unconv.spring.service.SensorLocationService;
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

    @Override
    public Optional<SensorLocation> findSensorLocationById(UUID id) {
        return sensorLocationRepository.findById(id);
    }

    @Override
    public SensorLocation saveSensorLocation(SensorLocation sensorLocation) {
        return sensorLocationRepository.save(sensorLocation);
    }

    @Override
    public void deleteSensorLocationById(UUID id) {
        sensorLocationRepository.deleteById(id);
    }
}
