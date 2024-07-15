package com.unconv.spring.service;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

/** Service interface for managing {@link SensorSystem}s. */
public interface SensorSystemService {

    /**
     * Retrieves a paginated list of all SensorSystems.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of SensorSystemDTOs.
     */
    PagedResult<SensorSystemDTO> findAllSensorSystems(
            int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves a paginated list of SensorSystems by UnconvUserId.
     *
     * @param unconvUserId The ID of the UnconvUser.
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of SensorSystemDTOs.
     */
    PagedResult<SensorSystemDTO> findAllSensorSystemsByUnconvUserId(
            UUID unconvUserId, int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves a SensorSystem by its ID.
     *
     * @param id The ID of the SensorSystem.
     * @return An Optional containing the SensorSystem, or empty if not found.
     */
    Optional<SensorSystem> findSensorSystemById(UUID id);

    /**
     * Retrieves a SensorSystemDTO by its ID.
     *
     * @param id The ID of the SensorSystem.
     * @return An Optional containing the SensorSystemDTO, or empty if not found.
     */
    Optional<SensorSystemDTO> findSensorSystemDTOById(UUID id);

    boolean isActiveSensorSystem(SensorSystem sensorSystem);

    /**
     * Saves a new SensorSystem.
     *
     * @param sensorSystem The SensorSystem to save.
     * @return The saved SensorSystem.
     */
    SensorSystem saveSensorSystem(SensorSystem sensorSystem);

    /**
     * Validates the UnconvUser and saves a new SensorSystem.
     *
     * @param sensorSystemDTO The SensorSystemDTO to save.
     * @param authentication The authentication object.
     * @return ResponseEntity containing a MessageResponse with the saved SensorSystemDTO.
     */
    ResponseEntity<MessageResponse<SensorSystemDTO>> validateUnconvUserAndSaveSensorSystem(
            SensorSystemDTO sensorSystemDTO, Authentication authentication);

    /**
     * Deletes a SensorSystem by its ID.
     *
     * @param id The ID of the SensorSystem to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    boolean deleteSensorSystemById(UUID id);

    /**
     * Retrieves a list of SensorSystems by sensor name.
     *
     * @param sensorName The name of the sensor.
     * @return A list of SensorSystems with the specified sensor name.
     */
    List<SensorSystem> findAllSensorSystemsBySensorName(String sensorName);

    /**
     * Retrieves a list of SensorSystems by sensor name and UnconvUserId.
     *
     * @param sensorName The name of the sensor.
     * @param unconvUserId The ID of the UnconvUser.
     * @return A list of SensorSystems with the specified sensor name and UnconvUserId.
     */
    List<SensorSystem> findAllBySensorSystemsBySensorNameAndUnconvUserId(
            String sensorName, UUID unconvUserId);

    /**
     * Finds recent statistics by SensorSystem ID.
     *
     * @param sensorSystemId The ID of the SensorSystem.
     * @return A map containing recent statistics for the SensorSystem.
     */
    Map<Integer, Long> findRecentStatsBySensorSystemId(UUID sensorSystemId);
}
