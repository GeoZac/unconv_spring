package com.unconv.spring.service.impl;

import static com.unconv.spring.consts.MessageConstants.ENVT_FILE_FORMAT_ERROR;
import static com.unconv.spring.consts.MessageConstants.ENVT_FILE_REJ_ERR;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_DLTD;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_INAT;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_USER;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.utils.CSVUtil;
import java.io.IOException;
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
    public List<EnvironmentalReading> findLatestEnvironmentalReadingsByUnconvUserId(UUID id) {
        return environmentalReadingRepository
                .findFirst10BySensorSystemUnconvUserIdOrderByTimestampDesc(id);
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
}
