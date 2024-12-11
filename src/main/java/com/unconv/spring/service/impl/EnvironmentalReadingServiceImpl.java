package com.unconv.spring.service.impl;

import static com.unconv.spring.consts.AppConstants.MAX_PAGE_SIZE;
import static com.unconv.spring.consts.MessageConstants.ENVT_FILE_FORMAT_ERROR;
import static com.unconv.spring.consts.MessageConstants.ENVT_FILE_REJ_ERR;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_DLTD;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_INAT;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_USER;
import static java.lang.Math.min;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.model.response.ExtremeReadingsResponse;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.utils.CSVUtil;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class EnvironmentalReadingServiceImpl implements EnvironmentalReadingService {

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private ModelMapper modelMapper;

    /**
     * Retrieves a paginated list of all EnvironmentalReadings.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of EnvironmentalReadings.
     */
    @Override
    public PagedResult<EnvironmentalReading> findAllEnvironmentalReadings(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, min(pageSize, MAX_PAGE_SIZE), sort);
        Page<EnvironmentalReading> environmentalReadingsPage =
                environmentalReadingRepository.findAll(pageable);

        return new PagedResult<>(environmentalReadingsPage);
    }

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
    @Override
    public PagedResult<EnvironmentalReading> findAllEnvironmentalReadingsBySensorSystemId(
            UUID sensorSystemId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, min(pageSize, MAX_PAGE_SIZE), sort);
        Page<EnvironmentalReading> environmentalReadingsPage =
                environmentalReadingRepository.findAllBySensorSystemId(sensorSystemId, pageable);

        return new PagedResult<>(environmentalReadingsPage);
    }

    /**
     * Finds a list of {@link EnvironmentalReading} entities associated with a specific sensor
     * system within a given time range.
     *
     * @param sensorSystemId the unique identifier of the sensor system
     * @param startTime the start of the time range as an {@link OffsetDateTime}
     * @param endTime the end of the time range as an {@link OffsetDateTime}
     * @return a list of {@link EnvironmentalReading} entities that match the sensor system ID and
     *     fall within the specified time range
     */
    @Override
    public List<EnvironmentalReading> findBySensorSystemIdAndTimestampBetween(
            UUID sensorSystemId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                sensorSystemId, startTime, endTime);
    }

    /**
     * Retrieves an EnvironmentalReading by its ID.
     *
     * @param id The ID of the EnvironmentalReading.
     * @return An Optional containing the EnvironmentalReading, or empty if not found.
     */
    @Override
    public Optional<EnvironmentalReading> findEnvironmentalReadingById(UUID id) {
        return environmentalReadingRepository.findById(id);
    }

    /**
     * Retrieves the latest EnvironmentalReadings for a given UnconvUser.
     *
     * @param id The ID of the UnconvUser.
     * @return A list of latest EnvironmentalReadings.
     */
    @Override
    public List<EnvironmentalReading> findLatestEnvironmentalReadingsByUnconvUserId(UUID id) {
        return environmentalReadingRepository
                .findFirst10BySensorSystemUnconvUserIdOrderByTimestampDesc(id);
    }

    /**
     * Saves a new EnvironmentalReading.
     *
     * @param environmentalReading The EnvironmentalReading to save.
     * @return The saved EnvironmentalReading.
     */
    @Override
    public EnvironmentalReading saveEnvironmentalReading(
            EnvironmentalReading environmentalReading) {
        return environmentalReadingRepository.save(environmentalReading);
    }

    /**
     * Validates the UnconvUser, generates timestamp if required, and saves a new
     * EnvironmentalReading.
     *
     * @param environmentalReadingDTO The EnvironmentalReadingDTO to save.
     * @param authentication The authentication object.
     * @return ResponseEntity containing a MessageResponse with the saved EnvironmentalReadingDTO.
     */
    @Override
    public ResponseEntity<MessageResponse<EnvironmentalReadingDTO>>
            generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                    EnvironmentalReadingDTO environmentalReadingDTO,
                    Authentication authentication) {

        SensorSystem sensorSystem =
                sensorSystemRepository.findSensorSystemById(
                        environmentalReadingDTO.getSensorSystem().getId());

        if (!sensorSystem.getUnconvUser().getUsername().equals(authentication.getName())) {
            MessageResponse<EnvironmentalReadingDTO> environmentalReadingDTOMessageResponse =
                    new MessageResponse<>(environmentalReadingDTO, ENVT_RECORD_REJ_USER);
            return new ResponseEntity<>(
                    environmentalReadingDTOMessageResponse, HttpStatus.UNAUTHORIZED);
        }

        if (sensorSystem.isDeleted()) {
            MessageResponse<EnvironmentalReadingDTO> environmentalReadingDTOMessageResponse =
                    new MessageResponse<>(environmentalReadingDTO, ENVT_RECORD_REJ_DLTD);
            return new ResponseEntity<>(
                    environmentalReadingDTOMessageResponse, HttpStatus.BAD_REQUEST);
        }

        if (sensorSystem.getSensorStatus() != SensorStatus.ACTIVE) {
            MessageResponse<EnvironmentalReadingDTO> environmentalReadingDTOMessageResponse =
                    new MessageResponse<>(environmentalReadingDTO, ENVT_RECORD_REJ_INAT);
            return new ResponseEntity<>(
                    environmentalReadingDTOMessageResponse, HttpStatus.BAD_REQUEST);
        }

        if (environmentalReadingDTO.getTimestamp() == null) {
            environmentalReadingDTO.setTimestamp();
        }

        EnvironmentalReading environmentalReading =
                saveEnvironmentalReading(
                        modelMapper.map(environmentalReadingDTO, EnvironmentalReading.class));

        MessageResponse<EnvironmentalReadingDTO> environmentalReadingDTOMessageResponse =
                new MessageResponse<>(
                        modelMapper.map(environmentalReading, EnvironmentalReadingDTO.class),
                        ENVT_RECORD_ACCEPTED);
        return new ResponseEntity<>(environmentalReadingDTOMessageResponse, HttpStatus.CREATED);
    }

    /**
     * Parses EnvironmentalReadings from a CSV file and saves them for the given SensorSystem.
     *
     * @param file The CSV file containing EnvironmentalReadings.
     * @param sensorSystem The SensorSystem to associate with the readings.
     * @return The number of readings successfully parsed and saved.
     */
    @Override
    public int parseFromCSVAndSaveEnvironmentalReading(
            MultipartFile file, SensorSystem sensorSystem) {
        try {
            List<EnvironmentalReading> environmentalReadings =
                    CSVUtil.csvToEnvironmentalReadings(file.getInputStream(), sensorSystem);
            List<EnvironmentalReading> savedEnvironmentalReadings =
                    environmentalReadingRepository.saveAll(environmentalReadings);
            return savedEnvironmentalReadings.size();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file data" + e.getMessage());
        }
    }

    /**
     * Deletes an EnvironmentalReading by its ID.
     *
     * @param id The ID of the EnvironmentalReading to delete.
     */
    @Override
    public void deleteEnvironmentalReadingById(UUID id) {
        environmentalReadingRepository.deleteById(id);
    }

    /**
     * Verifies the CSV file, validates the SensorSystem, and parses EnvironmentalReadings.
     *
     * @param sensorSystem The SensorSystem to validate and associate with the readings.
     * @param file The CSV file containing EnvironmentalReadings.
     * @return ResponseEntity containing a message indicating the success or failure of the
     *     operation.
     */
    @Override
    public ResponseEntity<String> verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings(
            SensorSystem sensorSystem, MultipartFile file) {
        String message;

        if (CSVUtil.isOfCSVFormat(file)) {
            try {
                int recordsProcessed = parseFromCSVAndSaveEnvironmentalReading(file, sensorSystem);

                message =
                        "Uploaded the file successfully: "
                                + file.getOriginalFilename()
                                + " with "
                                + recordsProcessed
                                + " records";
                return ResponseEntity.status(HttpStatus.CREATED).body(message);
            } catch (Exception e) {
                message = String.format(ENVT_FILE_REJ_ERR, file.getOriginalFilename());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
            }
        }

        message = ENVT_FILE_FORMAT_ERROR;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @Override
    public ExtremeReadingsResponse getExtremeReadingsResponseBySensorSystemId(UUID sensorSystemId) {
        return new ExtremeReadingsResponse(
                environmentalReadingRepository.findFirstBySensorSystemIdOrderByTemperatureDesc(
                        sensorSystemId),
                environmentalReadingRepository.findFirstBySensorSystemIdOrderByTemperatureAsc(
                        sensorSystemId),
                environmentalReadingRepository.findFirstBySensorSystemIdOrderByHumidityDesc(
                        sensorSystemId),
                environmentalReadingRepository.findFirstBySensorSystemIdOrderByHumidityAsc(
                        sensorSystemId));
    }
}
