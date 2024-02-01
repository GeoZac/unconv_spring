package com.unconv.spring.security.filter;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SensorAuthTokenUtil {

    private final SensorAuthTokenRepository sensorAuthTokenRepository;

    @Autowired
    public SensorAuthTokenUtil(SensorAuthTokenRepository sensorAuthTokenRepository) {
        this.sensorAuthTokenRepository = sensorAuthTokenRepository;
    }

    String validateTokenAndRetrieveUser(String accessToken) {
        SensorAuthToken sensorAuthToken = sensorAuthTokenRepository.findByAuthToken(accessToken);

        if (sensorAuthToken == null) {
            log.error("Request with unknown API token");
            return null;
        }

        return sensorAuthToken.getSensorSystem().getUnconvUser().getUsername();
    }
}
