package com.unconv.spring.web.rest;

import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_SENS;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.model.response.ExtremeReadingsResponse;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.service.SensorSystemService;
import com.unconv.spring.service.UnconvUserService;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller class responsible for handling HTTP requests related to {@link EnvironmentalReading}.
 * It provides endpoints for managing sensor systems.
 */
@RestController
@RequestMapping("/EnvironmentalReading")
@Slf4j
public class EnvironmentalReadingController {

    private final EnvironmentalReadingService environmentalReadingService;

    private final SensorSystemService sensorSystemService;

    private final UnconvUserService unconvUserService;

    private final ModelMapper modelMapper;

    /**
     * Constructs an {@link EnvironmentalReadingController} with the specified services and model
     * mapper.
     *
     * @param environmentalReadingService the service to manage environmental readings
     * @param sensorSystemService the service to manage sensor systems
     * @param unconvUserService the service to manage user-specific functionalities
     * @param modelMapper the mapper to convert between DTOs and entities
     */
    public EnvironmentalReadingController(
            EnvironmentalReadingService environmentalReadingService,
            SensorSystemService sensorSystemService,
            UnconvUserService unconvUserService,
            ModelMapper modelMapper) {
        this.environmentalReadingService = environmentalReadingService;
        this.sensorSystemService = sensorSystemService;
        this.unconvUserService = unconvUserService;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves a paginated list of environmental readings.
     *
     * @param pageNo The page number to retrieve (default is 0).
     * @param pageSize The size of each page (default is 10).
     * @param sortBy The field to sort by (default is "createdAt").
     * @param sortDir The direction of sorting (default is "desc" for descending).
     * @return A {@link PagedResult} containing the paginated list of {@link EnvironmentalReading}s.
     */
    @GetMapping
    public PagedResult<EnvironmentalReading> getAllEnvironmentalReadings(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_ER_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_ER_SORT_DIRECTION, required = false)
                    String sortDir) {
        return environmentalReadingService.findAllEnvironmentalReadings(
                pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Retrieves a paginated list of EnvironmentalReadings associated with a specific SensorSystem
     * identified by its ID.
     *
     * @param sensorSystemId The ID of the SensorSystem whose EnvironmentalReadings are to be
     *     retrieved.
     * @param pageNo The page number to retrieve (default is 0).
     * @param pageSize The size of each page (default is 10).
     * @param sortBy The field to sort by (default is "createdAt").
     * @param sortDir The direction of sorting (default is "desc" for descending).
     * @return A PagedResult containing the paginated list of EnvironmentalReadings associated with
     *     the specified SensorSystem.
     */
    @GetMapping("SensorSystem/{sensorSystemId}")
    public PagedResult<EnvironmentalReading> getAllEnvironmentalReadingsBySensorSystemId(
            @PathVariable UUID sensorSystemId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_ER_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_ER_SORT_DIRECTION, required = false)
                    String sortDir) {
        return environmentalReadingService.findAllEnvironmentalReadingsBySensorSystemId(
                sensorSystemId, pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Retrieves environmental readings for a specified sensor system within the last specified
     * interval. If no interval (hours) is provided, the default interval is the past 24 hours.
     *
     * @param hours an optional number of hours representing the time interval to look back from the
     *     current time; if omitted, defaults to 24 hours
     * @param sensorSystemId the unique identifier of the sensor system for which readings are
     *     requested
     * @return a {@link ResponseEntity} containing a list of {@link EnvironmentalReading} entities
     *     if the sensor system exists, or a {@link ResponseEntity#notFound()} status if it does not
     */
    @GetMapping("Interval/SensorSystem/{sensorSystemId}")
    public ResponseEntity<List<EnvironmentalReading>> getReadingsInLastInterval(
            @RequestParam(required = false) Integer hours, @PathVariable UUID sensorSystemId) {
        return sensorSystemService
                .findSensorSystemById(sensorSystemId)
                .map(
                        sensorSystem -> {
                            OffsetDateTime now = OffsetDateTime.now();
                            OffsetDateTime startTime;

                            startTime = hours != null ? now.minusHours(hours) : now.minusDays(1);

                            List<EnvironmentalReading> readings =
                                    environmentalReadingService
                                            .findBySensorSystemIdAndTimestampBetween(
                                                    sensorSystemId, startTime, now);

                            return ResponseEntity.ok(readings);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves an EnvironmentalReading by its ID.
     *
     * @param id The ID of the EnvironmentalReading to retrieve.
     * @return ResponseEntity with status 200 (OK) and the retrieved EnvironmentalReading if found,
     *     or ResponseEntity with status 404 (Not Found) if no EnvironmentalReading with the given
     *     ID exists.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentalReading> getEnvironmentalReadingById(@PathVariable UUID id) {
        return environmentalReadingService
                .findEnvironmentalReadingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves the latest EnvironmentalReadings associated with a specific unconverted user
     * identified by the given ID.
     *
     * @param unconvUserId The ID of the unconverted user whose latest EnvironmentalReadings are to
     *     be retrieved.
     * @return A List of EnvironmentalReading objects representing the latest readings for the
     *     specified unconverted user.
     */
    @GetMapping("/Latest/UnconvUser/{unconvUserId}")
    public ResponseEntity<List<EnvironmentalReading>> findLatestEnvironmentalReadingsByUnconvUser(
            @PathVariable UUID unconvUserId) {
        return unconvUserService
                .findUnconvUserById(unconvUserId)
                .map(
                        unconvUser ->
                                ResponseEntity.ok(
                                        environmentalReadingService
                                                .findLatestEnvironmentalReadingsByUnconvUserId(
                                                        unconvUserId)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves the extreme readings (highest and lowest temperature, highest and lowest humidity)
     * for a specific sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return an HTTP response containing the extreme readings, or a 404 Not Found response if the
     *     sensor system is not found
     */
    @GetMapping("/Extreme/SensorSystem/{sensorSystemId}")
    public ResponseEntity<ExtremeReadingsResponse> getExtremeReadingsResponseBySensorSystemId(
            @PathVariable UUID sensorSystemId) {
        return sensorSystemService
                .findSensorSystemById(sensorSystemId)
                .map(
                        sensorSystem ->
                                ResponseEntity.ok(
                                        environmentalReadingService
                                                .getExtremeReadingsResponseBySensorSystemId(
                                                        sensorSystemId)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new EnvironmentalReading based on the provided EnvironmentalReadingDTO.
     *
     * @param environmentalReadingDTO The EnvironmentalReadingDTO containing the data for the new
     *     EnvironmentalReading.
     * @param authentication Represents the authenticated user making the request.
     * @return ResponseEntity containing a MessageResponse with the created EnvironmentalReadingDTO
     *     if successful, or a ResponseEntity with status 404 (Not Found) if the associated
     *     SensorSystem is not found.
     */
    @PostMapping
    public ResponseEntity<MessageResponse<EnvironmentalReadingDTO>> createEnvironmentalReading(
            @RequestBody @Validated EnvironmentalReadingDTO environmentalReadingDTO,
            Authentication authentication) {
        environmentalReadingDTO.setId(null);
        return sensorSystemService
                .findSensorSystemById(environmentalReadingDTO.getSensorSystem().getId())
                .map(
                        (sensorSystem ->
                                environmentalReadingService
                                        .generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                                                environmentalReadingDTO, authentication)))
                .orElseGet(
                        () -> {
                            MessageResponse<EnvironmentalReadingDTO>
                                    environmentalReadingDTOMessageResponse =
                                            new MessageResponse<>(
                                                    environmentalReadingDTO, ENVT_RECORD_REJ_SENS);
                            return new ResponseEntity<>(
                                    environmentalReadingDTOMessageResponse, HttpStatus.NOT_FOUND);
                        });
    }

    /**
     * Handles the file upload for environmental readings.
     *
     * @param sensorSystemId the ID of the sensor system
     * @param file the multipart file containing the environmental readings data
     * @return ResponseEntity with a status and message indicating the result of the upload
     *     operation
     */
    @PostMapping("/Bulk/SensorSystem/{sensorSystemId}")
    public ResponseEntity<String> uploadFile(
            @PathVariable UUID sensorSystemId, @RequestParam MultipartFile file) {
        return sensorSystemService
                .findSensorSystemById(sensorSystemId)
                .map(
                        (sensorSystem ->
                                environmentalReadingService
                                        .verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings(
                                                sensorSystem, file)))
                .orElseGet(
                        () ->
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(ENVT_RECORD_REJ_SENS));
    }

    /**
     * Updates an environmental reading.
     *
     * @param id the ID of the environmental reading to update
     * @param environmentalReadingDTO the DTO containing the updated environmental reading data
     * @return ResponseEntity containing the updated environmental reading object
     */
    @PutMapping("/{id}")
    public ResponseEntity<EnvironmentalReading> updateEnvironmentalReading(
            @PathVariable UUID id,
            @RequestBody @Valid EnvironmentalReadingDTO environmentalReadingDTO) {
        return environmentalReadingService
                .findEnvironmentalReadingById(id)
                .map(
                        environmentalReadingObj -> {
                            environmentalReadingDTO.setId(id);
                            return ResponseEntity.ok(
                                    environmentalReadingService.saveEnvironmentalReading(
                                            modelMapper.map(
                                                    environmentalReadingDTO,
                                                    EnvironmentalReading.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes an EnvironmentalReading identified by the given ID.
     *
     * @param id The ID of the EnvironmentalReading to delete.
     * @return ResponseEntity with status 200 (OK) and the deleted EnvironmentalReading if found and
     *     deleted successfully, or ResponseEntity with status 404 (Not Found) if no
     *     EnvironmentalReading with the given ID exists.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<EnvironmentalReading> deleteEnvironmentalReading(@PathVariable UUID id) {
        return environmentalReadingService
                .findEnvironmentalReadingById(id)
                .map(
                        environmentalReading -> {
                            environmentalReadingService.deleteEnvironmentalReadingById(id);
                            return ResponseEntity.ok(environmentalReading);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
