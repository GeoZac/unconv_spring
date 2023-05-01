package com.unconv.spring.service;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorLocationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SensorLocationService {

    @Autowired private SensorLocationRepository sensorLocationRepository;

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

    public Optional<SensorLocation> findSensorLocationById(UUID id) {
        return sensorLocationRepository.findById(id);
    }

    public SensorLocation saveSensorLocation(SensorLocation sensorLocation) {
        return sensorLocationRepository.save(sensorLocation);
    }

    public void deleteSensorLocationById(UUID id) {
        sensorLocationRepository.deleteById(id);
    }
}
