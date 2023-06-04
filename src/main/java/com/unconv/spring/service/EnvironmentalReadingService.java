package com.unconv.spring.service;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
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

@Service
@Transactional
public class EnvironmentalReadingService {

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private ModelMapper modelMapper;

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

    public Optional<EnvironmentalReading> findEnvironmentalReadingById(UUID id) {
        return environmentalReadingRepository.findById(id);
    }

    public EnvironmentalReading saveEnvironmentalReading(
            EnvironmentalReading environmentalReading) {
        return environmentalReadingRepository.save(environmentalReading);
    }

    public ResponseEntity<MessageResponse<EnvironmentalReadingDTO>>
            generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                    EnvironmentalReadingDTO environmentalReadingDTO,
                    Authentication authenticaation) {
        if (environmentalReadingDTO.getTimestamp() == null) {
            environmentalReadingDTO.setTimestamp();
        }
        EnvironmentalReading environmentalReading =
                saveEnvironmentalReading(
                        modelMapper.map(environmentalReadingDTO, EnvironmentalReading.class));

        MessageResponse<EnvironmentalReadingDTO> environmentalReadingDTOMessageResponse =
                new MessageResponse<>(
                        modelMapper.map(environmentalReading, EnvironmentalReadingDTO.class),
                        "Record added successfully");
        return new ResponseEntity<>(environmentalReadingDTOMessageResponse, HttpStatus.CREATED);
    }

    public void deleteEnvironmentalReadingById(UUID id) {
        environmentalReadingRepository.deleteById(id);
    }

    public Map<OffsetDateTime, Double> getAverageTempsForQuarterHourly(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(3),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForQuarterHourly(data));
    }

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

    public Map<OffsetDateTime, Double> getAverageTempsForHourly(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(24),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForHourly(data));
    }

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

    public Map<OffsetDateTime, Double> getAverageTempsForDaily(UUID sensorSystemId) {

        List<EnvironmentalReading> data =
                environmentalReadingRepository.findBySensorSystemIdAndTimestampBetween(
                        sensorSystemId,
                        OffsetDateTime.now(ZoneOffset.UTC).minusDays(7),
                        OffsetDateTime.now(ZoneOffset.UTC));

        return new TreeMap<>(getAverageTempsForDaily(data));
    }

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
}
