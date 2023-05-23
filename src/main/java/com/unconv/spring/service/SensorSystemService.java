package com.unconv.spring.service;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorSystemRepository;

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
public class SensorSystemService {

    @Autowired private SensorSystemRepository sensorSystemRepository;

    public PagedResult<SensorSystem> findAllSensorSystems(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<SensorSystem> sensorSystemsPage = sensorSystemRepository.findAll(pageable);

        return new PagedResult<>(sensorSystemsPage);
    }

    public PagedResult<SensorSystem> findAllSensorSystemsByUnconvUserId(
            UUID unconvUserId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<SensorSystem> sensorSystemsPage =
                sensorSystemRepository.findAllByUnconvUserId(unconvUserId, pageable);

        return new PagedResult<>(sensorSystemsPage);
    }

    public Optional<SensorSystem> findSensorSystemById(UUID id) {
        return sensorSystemRepository.findById(id);
    }

    public SensorSystem saveSensorSystem(SensorSystem sensorSystem) {
        return sensorSystemRepository.save(sensorSystem);
    }

    public void deleteSensorSystemById(UUID id) {
        sensorSystemRepository.deleteById(id);
    }
}
