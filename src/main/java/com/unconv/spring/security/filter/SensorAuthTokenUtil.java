package com.unconv.spring.security.filter;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.exception.MalformedAuthTokenException;
import com.unconv.spring.exception.UnknownAuthTokenException;
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
        String hashString = accessToken.substring(accessToken.length() - 24);
        SensorAuthToken sensorAuthToken =
                sensorAuthTokenRepository.findByTokenHashAllIgnoreCase(hashString);

        if (sensorAuthToken == null) {
            log.error("Request with unknown API token");
            throw new UnknownAuthTokenException(accessToken);
        }

        // Actually match the token
        if (!bCryptPasswordEncoder().matches(accessToken, sensorAuthToken.getAuthToken())) {
            log.error("Request with mismatched token");
            throw new MalformedAuthTokenException(accessToken);
        }

        return sensorAuthToken.getSensorSystem().getUnconvUser().getUsername();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
