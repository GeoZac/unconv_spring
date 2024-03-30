package com.unconv.spring.service;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorAuthTokenDTO;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

/** Service interface for managing SensorAuthTokens. */
public interface SensorAuthTokenService {

    /**
     * Retrieves a paginated list of SensorAuthTokens.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of SensorAuthTokens.
     */
    PagedResult<SensorAuthToken> findAllSensorAuthTokens(
            int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves a SensorAuthToken by its ID.
     *
     * @param id The ID of the SensorAuthToken.
     * @return An Optional containing the SensorAuthToken, or empty if not found.
     */
    Optional<SensorAuthToken> findSensorAuthTokenById(UUID id);

    /**
     * Saves a SensorAuthToken.
     *
     * @param sensorAuthToken The SensorAuthToken to save.
     * @return The saved SensorAuthToken.
     */
    SensorAuthToken saveSensorAuthToken(SensorAuthToken sensorAuthToken);

    /**
     * Deletes a SensorAuthToken by its ID.
     *
     * @param id The ID of the SensorAuthToken to delete.
     */
    void deleteSensorAuthTokenById(UUID id);

    /**
     * Generates an authentication token for the given SensorSystem.
     *
     * @param sensorSystemObj The SensorSystem for which the token is generated.
     * @return The generated SensorAuthTokenDTO.
     */
    SensorAuthTokenDTO generateSensorAuthToken(SensorSystem sensorSystemObj);

    /**
     * Retrieves information about the authentication token for the given SensorSystem.
     *
     * @param sensorSystem The SensorSystem for which to retrieve token information.
     * @return The SensorAuthTokenDTO containing token information.
     */
    SensorAuthTokenDTO getSensorAuthTokenInfo(SensorSystem sensorSystem);

    /**
     * Generates a unique salted suffix for authentication tokens.
     *
     * @return A unique salted suffix.
     */
    String generateUniqueSaltedSuffix();
}
