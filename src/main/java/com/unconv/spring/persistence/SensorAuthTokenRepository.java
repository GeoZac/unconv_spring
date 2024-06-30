package com.unconv.spring.persistence;

import com.unconv.spring.domain.SensorAuthToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link SensorAuthToken} entities. Extends {@link JpaRepository}
 * to inherit basic CRUD functionality.
 */
public interface SensorAuthTokenRepository extends JpaRepository<SensorAuthToken, UUID> {

    /**
     * Retrieve a sensor authentication token by its authentication token.
     *
     * @param authToken The authentication token.
     * @return The {@link SensorAuthToken} entity if found, otherwise {@code null}.
     */
    SensorAuthToken findByAuthToken(String authToken);

    /**
     * Retrieve a sensor authentication token by its token hash, ignoring case.
     *
     * @param tokenHash The token hash.
     * @return The {@link SensorAuthToken} entity if found, otherwise {@code null}.
     */
    SensorAuthToken findByTokenHashAllIgnoreCase(String tokenHash);

    /**
     * Retrieve a sensor authentication token by the ID of the associated sensor system.
     *
     * @param id The ID of the sensor system.
     * @return The {@link SensorAuthToken} entity if found, otherwise {@code null}.
     */
    SensorAuthToken findBySensorSystemId(UUID id);
}
