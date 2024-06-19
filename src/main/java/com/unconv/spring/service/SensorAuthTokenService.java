package com.unconv.spring.service;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorAuthTokenDTO;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

public interface SensorAuthTokenService {
    PagedResult<SensorAuthToken> findAllSensorAuthTokens(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<SensorAuthToken> findSensorAuthTokenById(UUID id);

    Optional<SensorAuthTokenDTO> findSensorAuthTokenDTOById(UUID id);

    SensorAuthToken saveSensorAuthToken(SensorAuthToken sensorAuthToken);

    SensorAuthTokenDTO saveSensorAuthTokenDTO(SensorAuthToken sensorAuthToken);

    void deleteSensorAuthTokenById(UUID id);

    void deleteAnyExistingSensorSystem(UUID sensorSystemId);

    SensorAuthTokenDTO generateSensorAuthToken(SensorSystem sensorSystemObj, UUID id);

    SensorAuthTokenDTO getSensorAuthTokenInfo(SensorSystem sensorSystem);

    String generateUniqueSaltedSuffix();
}
