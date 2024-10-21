package com.unconv.spring.service;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

/** Service interface for managing EnvironmentalReadings. */
public interface EnvironmentalReadingService {

    /**
     * Retrieves a paginated list of all EnvironmentalReadings.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of EnvironmentalReadings.
     */
    PagedResult<EnvironmentalReading> findAllEnvironmentalReadings(
            int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves a paginated list of EnvironmentalReadings by SensorSystem ID.
     *
     * @param sensorSystemId The ID of the SensorSystem.
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of EnvironmentalReadings.
     */
    PagedResult<EnvironmentalReading> findAllEnvironmentalReadingsBySensorSystemId(
            UUID sensorSystemId, int pageNo, int pageSize, String sortBy, String sortDir);

    List<EnvironmentalReading> findBySensorSystemIdAndTimestampBetween(
            UUID sensorSystemId, OffsetDateTime startTime, OffsetDateTime endTime);

    /**
     * Retrieves an EnvironmentalReading by its ID.
     *
     * @param id The ID of the EnvironmentalReading.
     * @return An Optional containing the EnvironmentalReading, or empty if not found.
     */
    Optional<EnvironmentalReading> findEnvironmentalReadingById(UUID id);

    /**
     * Retrieves the latest EnvironmentalReadings for a given UnconvUser.
     *
     * @param id The ID of the UnconvUser.
     * @return A list of latest EnvironmentalReadings.
     */
    List<EnvironmentalReading> findLatestEnvironmentalReadingsByUnconvUserId(UUID id);

    /**
     * Saves a new EnvironmentalReading.
     *
     * @param environmentalReading The EnvironmentalReading to save.
     * @return The saved EnvironmentalReading.
     */
    EnvironmentalReading saveEnvironmentalReading(EnvironmentalReading environmentalReading);

    /**
     * Validates the UnconvUser, generates timestamp if required, and saves a new
     * EnvironmentalReading.
     *
     * @param environmentalReadingDTO The EnvironmentalReadingDTO to save.
     * @param authentication The authentication object.
     * @return ResponseEntity containing a MessageResponse with the saved EnvironmentalReadingDTO.
     */
    ResponseEntity<MessageResponse<EnvironmentalReadingDTO>>
            generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                    EnvironmentalReadingDTO environmentalReadingDTO, Authentication authentication);

    /**
     * Parses EnvironmentalReadings from a CSV file and saves them for the given SensorSystem.
     *
     * @param file The CSV file containing EnvironmentalReadings.
     * @param sensorSystem The SensorSystem to associate with the readings.
     * @return The number of readings successfully parsed and saved.
     */
    int parseFromCSVAndSaveEnvironmentalReading(MultipartFile file, SensorSystem sensorSystem);

    /**
     * Deletes an EnvironmentalReading by its ID.
     *
     * @param id The ID of the EnvironmentalReading to delete.
     */
    void deleteEnvironmentalReadingById(UUID id);

    /**
     * Verifies the CSV file, validates the SensorSystem, and parses EnvironmentalReadings.
     *
     * @param sensorSystem The SensorSystem to validate and associate with the readings.
     * @param file The CSV file containing EnvironmentalReadings.
     * @return ResponseEntity containing a message indicating the success or failure of the
     *     operation.
     */
    ResponseEntity<String> verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings(
            SensorSystem sensorSystem, MultipartFile file);
}
