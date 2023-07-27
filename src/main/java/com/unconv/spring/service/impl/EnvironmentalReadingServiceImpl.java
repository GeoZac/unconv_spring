package com.unconv.spring.service.impl;

import static com.unconv.spring.consts.MessageConstants.ENVT_FILE_FORMAT_ERROR;
import static com.unconv.spring.consts.MessageConstants.ENVT_FILE_REJ_ERR;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_DLTD;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_INAT;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_SENS;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_USER;

import com.unconv.spring.consts.SensorStatus;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.utils.CSVUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
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

    @Override
    public PagedResult<EnvironmentalReading> findAllEnvironmentalReadings(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<EnvironmentalReading> environmentalReadingsPage =
                environmentalReadingRepository.findAll(pageable);

        return new PagedResult<>(environmentalReadingsPage);
    }

    @Override
    public PagedResult<EnvironmentalReading> findAllEnvironmentalReadingsBySensorSystemId(
            UUID sensorSystemId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<EnvironmentalReading> environmentalReadingsPage =
                environmentalReadingRepository.findAllBySensorSystemId(sensorSystemId, pageable);

        return new PagedResult<>(environmentalReadingsPage);
    }

    @Override
    public Optional<EnvironmentalReading> findEnvironmentalReadingById(UUID id) {
        return environmentalReadingRepository.findById(id);
    }

    @Override
    public EnvironmentalReading saveEnvironmentalReading(
            EnvironmentalReading environmentalReading) {
        return environmentalReadingRepository.save(environmentalReading);
    }

    @Override
    public ResponseEntity<MessageResponse<EnvironmentalReadingDTO>>
            generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                    EnvironmentalReadingDTO environmentalReadingDTO,
                    Authentication authentication) {

        Optional<SensorSystem> optionalSensorSystem =
                sensorSystemRepository.findById(environmentalReadingDTO.getSensorSystem().getId());

        if (optionalSensorSystem.isEmpty()) {
            MessageResponse<EnvironmentalReadingDTO> environmentalReadingDTOMessageResponse =
                    new MessageResponse<>(environmentalReadingDTO, ENVT_RECORD_REJ_SENS);
            return new ResponseEntity<>(
                    environmentalReadingDTOMessageResponse, HttpStatus.NOT_FOUND);
        }

        SensorSystem sensorSystem = optionalSensorSystem.get();
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

    @Override
    public void deleteEnvironmentalReadingById(UUID id) {
        environmentalReadingRepository.deleteById(id);
    }

    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(3),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForQuarterHourly(data));
    }

    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(
            List<EnvironmentalReading> data) {
        OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startTime = endTime.minusHours(3);
        Duration interval = Duration.ofMinutes(15);

        Map<OffsetDateTime, List<EnvironmentalReading>> groupedData =
                data.stream()
                        .filter(d -> d.getTimestamp().isAfter(startTime))
                        .collect(
                                Collectors.groupingBy(
                                        d -> roundTimeToInterval(d.getTimestamp(), interval)));

        return groupedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey, e -> calculateAverageTemp(e.getValue())));
    }

    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForHourly(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(24),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForHourly(data));
    }

    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForHourly(List<EnvironmentalReading> data) {
        OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startTime = endTime.minusHours(24);
        Duration interval = Duration.ofMinutes(60);

        Map<OffsetDateTime, List<EnvironmentalReading>> groupedData =
                data.stream()
                        .filter(d -> d.getTimestamp().isAfter(startTime))
                        .collect(
                                Collectors.groupingBy(
                                        d -> roundTimeToInterval(d.getTimestamp(), interval)));

        return groupedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey, e -> calculateAverageTemp(e.getValue())));
    }

    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForDaily(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusDays(7),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForDaily(data));
    }

    @Override
    public Map<OffsetDateTime, Double> getAverageTempsForDaily(List<EnvironmentalReading> data) {
        OffsetDateTime endTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startTime = endTime.minusDays(7);
        Duration interval = Duration.ofDays(1);

        Map<OffsetDateTime, List<EnvironmentalReading>> groupedData =
                data.stream()
                        .filter(d -> d.getTimestamp().isAfter(startTime))
                        .collect(
                                Collectors.groupingBy(
                                        d -> roundTimeToInterval(d.getTimestamp(), interval)));

        return groupedData.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey, e -> calculateAverageTemp(e.getValue())));
    }

    private OffsetDateTime roundTimeToInterval(OffsetDateTime dateTime, Duration interval) {
        long seconds = dateTime.toEpochSecond() / interval.getSeconds() * interval.getSeconds();
        Instant instant = Instant.ofEpochSecond(seconds);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private double calculateAverageTemp(List<EnvironmentalReading> data) {
        double sum = data.stream().mapToDouble(EnvironmentalReading::getTemperature).sum();
        return BigDecimal.valueOf(sum / data.size())
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public ResponseEntity<String> verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings(
            UUID sensorSystemId, MultipartFile file) {
        String message;
        final Optional<SensorSystem> sensorSystem = sensorSystemRepository.findById(sensorSystemId);

        if (sensorSystem.isEmpty()) {
            message = ENVT_RECORD_REJ_SENS;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        if (CSVUtil.isOfCSVFormat(file)) {
            try {
                int recordsProcessed =
                        parseFromCSVAndSaveEnvironmentalReading(file, sensorSystem.get());

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
}
