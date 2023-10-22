package com.unconv.spring.service;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface EnvironmentalReadingService {
    PagedResult<EnvironmentalReading> findAllEnvironmentalReadings(
            int pageNo, int pageSize, String sortBy, String sortDir);

    PagedResult<EnvironmentalReading> findAllEnvironmentalReadingsBySensorSystemId(
            UUID sensorSystemId, int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<EnvironmentalReading> findEnvironmentalReadingById(UUID id);

    List<EnvironmentalReading> findLatestEnvironmentalReadingsByUnconvUserId(UUID id);

    EnvironmentalReading saveEnvironmentalReading(EnvironmentalReading environmentalReading);

    ResponseEntity<MessageResponse<EnvironmentalReadingDTO>>
            generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                    EnvironmentalReadingDTO environmentalReadingDTO, Authentication authentication);

    int parseFromCSVAndSaveEnvironmentalReading(MultipartFile file, SensorSystem sensorSystem);

    void deleteEnvironmentalReadingById(UUID id);

    ResponseEntity<String> verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings(
            UUID sensorSystemId, MultipartFile file);
}
