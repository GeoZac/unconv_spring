package com.unconv.spring.service;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

public interface SensorSystemService {
    PagedResult<SensorSystem> findAllSensorSystems(
            int pageNo, int pageSize, String sortBy, String sortDir);

    PagedResult<SensorSystem> findAllSensorSystemsByUnconvUserId(
            UUID unconvUserId, int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<SensorSystem> findSensorSystemById(UUID id);

    Optional<SensorSystemDTO> findSensorSystemDTOById(UUID id);

    SensorSystem saveSensorSystem(SensorSystem sensorSystem);

    void deleteSensorSystemById(UUID id);
}
