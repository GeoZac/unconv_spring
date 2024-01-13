package com.unconv.spring.service;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

public interface SensorAuthTokenService {
    PagedResult<SensorAuthToken> findAllSensorAuthTokens(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<SensorAuthToken> findSensorAuthTokenById(UUID id);

    SensorAuthToken saveSensorAuthToken(SensorAuthToken sensorAuthToken);

    void deleteSensorAuthTokenById(UUID id);
}
