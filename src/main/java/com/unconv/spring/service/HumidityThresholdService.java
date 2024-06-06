package com.unconv.spring.service;

import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

/** Service interface for managing {@link HumidityThreshold}s. */
public interface HumidityThresholdService {
    PagedResult<HumidityThreshold> findAllHumidityThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<HumidityThreshold> findHumidityThresholdById(UUID id);

    HumidityThreshold saveHumidityThreshold(HumidityThreshold humidtiyThreshold);

    void deleteHumidityThresholdById(UUID id);
}
