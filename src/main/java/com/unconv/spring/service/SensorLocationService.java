package com.unconv.spring.service;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.model.response.PagedResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorLocationService {
    PagedResult<SensorLocation> findAllSensorLocations(
            int pageNo, int pageSize, String sortBy, String sortDir);

    List<SensorLocation> findAllSensorLocationsByUnconvUserId(UUID unconvUserId);

    Optional<SensorLocation> findSensorLocationById(UUID id);

    SensorLocation saveSensorLocation(SensorLocation sensorLocation);

    void deleteSensorLocationById(UUID id);
}
