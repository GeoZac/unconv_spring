package com.unconv.spring.service;

import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

public interface TemperatureThresholdService {
    PagedResult<TemperatureThreshold> findAllTemperatureThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<TemperatureThreshold> findTemperatureThresholdById(UUID id);

    TemperatureThreshold saveTemperatureThreshold(TemperatureThreshold temperatureThreshold);

    void deleteTemperatureThresholdById(UUID id);
}
