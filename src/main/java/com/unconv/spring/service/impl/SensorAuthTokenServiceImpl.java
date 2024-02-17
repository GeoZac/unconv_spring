package com.unconv.spring.service.impl;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import com.unconv.spring.service.SensorAuthTokenService;
import com.unconv.spring.utils.AccessTokenGenerator;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SensorAuthTokenServiceImpl implements SensorAuthTokenService {

    private final SensorAuthTokenRepository sensorAuthTokenRepository;

    @Autowired
    public SensorAuthTokenServiceImpl(SensorAuthTokenRepository sensorAuthTokenRepository) {
        this.sensorAuthTokenRepository = sensorAuthTokenRepository;
    }

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
        sensorAuthToken.setAuthToken(
                bCryptPasswordEncoder().encode(sensorAuthToken.getAuthToken()));
        sensorAuthToken.setExpiry(OffsetDateTime.now().plusDays(60));
        return sensorAuthTokenRepository.saveAndFlush(sensorAuthToken);
    }

    @Override
    public void deleteSensorAuthTokenById(UUID id) {
        sensorAuthTokenRepository.deleteById(id);
    }

    @Override
    public SensorAuthToken generateSensorAuthToken(SensorSystem sensorSystem) {
        SensorAuthToken sensorAuthToken = new SensorAuthToken();
        sensorAuthToken.setSensorSystem(sensorSystem);
        return saveSensorAuthToken(sensorAuthToken);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
