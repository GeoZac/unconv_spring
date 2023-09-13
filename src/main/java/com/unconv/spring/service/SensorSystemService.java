package com.unconv.spring.service;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface SensorSystemService {
    PagedResult<SensorSystemDTO> findAllSensorSystems(
            int pageNo, int pageSize, String sortBy, String sortDir);

    PagedResult<SensorSystemDTO> findAllSensorSystemsByUnconvUserId(
            UUID unconvUserId, int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<SensorSystem> findSensorSystemById(UUID id);

    Optional<SensorSystemDTO> findSensorSystemDTOById(UUID id);

    SensorSystem saveSensorSystem(SensorSystem sensorSystem);

    ResponseEntity<MessageResponse<SensorSystemDTO>> validateUnconvUserAndSaveSensorSystem(
            SensorSystemDTO sensorSystemDTO);

    boolean deleteSensorSystemById(UUID id);

    List<SensorSystem> findAllSensorSystemsBySensorName(String sensorName);

    List<SensorSystem> findAllBySensorSystemsBySensorNameAndUnconvUserId(
            String sensorName, UUID unconvUserId);

    Map<Integer, Long> findRecentStatsBySensorSystemId(UUID sensorSystemId);
}
