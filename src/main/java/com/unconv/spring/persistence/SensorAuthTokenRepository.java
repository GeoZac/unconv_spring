package com.unconv.spring.persistence;

import com.unconv.spring.domain.SensorAuthToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorAuthTokenRepository extends JpaRepository<SensorAuthToken, UUID> {
    SensorAuthToken findByAuthToken(String authToken);
}
