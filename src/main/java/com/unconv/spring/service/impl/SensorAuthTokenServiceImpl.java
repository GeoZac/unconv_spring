package com.unconv.spring.service.impl;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import com.unconv.spring.service.SensorAuthTokenService;
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
public class SensorAuthTokenServiceImpl implements SensorAuthTokenService {

    @Autowired private SensorAuthTokenRepository sensorAuthTokenRepository;

    @Override
    public PagedResult<SensorAuthToken> findAllSensorAuthTokens(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<SensorAuthToken> sensorAuthTokensPage = sensorAuthTokenRepository.findAll(pageable);

        return new PagedResult<>(sensorAuthTokensPage);
    }

    @Override
    public Optional<SensorAuthToken> findSensorAuthTokenById(UUID id) {
        return sensorAuthTokenRepository.findById(id);
    }

    @Override
    public SensorAuthToken saveSensorAuthToken(SensorAuthToken sensorAuthToken) {
        return sensorAuthTokenRepository.save(sensorAuthToken);
    }

    @Override
    public void deleteSensorAuthTokenById(UUID id) {
        sensorAuthTokenRepository.deleteById(id);
    }
}
