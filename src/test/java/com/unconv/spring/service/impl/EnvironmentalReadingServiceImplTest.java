package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class EnvironmentalReadingServiceImplTest {

    @Mock private EnvironmentalReadingRepository environmentalReadingRepository;

    @InjectMocks private EnvironmentalReadingServiceImpl environmentalReadingService;

    private EnvironmentalReading environmentalReading;
    private UUID environmentalReadingId;

    @BeforeEach
    void setUp() {
        environmentalReadingId = UUID.randomUUID();
        environmentalReading = new EnvironmentalReading();
        environmentalReading.setId(environmentalReadingId);
    }

    @Test
    void findAllEnvironmentalReadings() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        List<EnvironmentalReading> environmentalReadingList =
                Collections.singletonList(environmentalReading);
        Page<EnvironmentalReading> environmentalReadingPage =
                new PageImpl<>(environmentalReadingList);

        when(environmentalReadingRepository.findAll(any(Pageable.class)))
                .thenReturn(environmentalReadingPage);

        PagedResult<EnvironmentalReading> result =
                environmentalReadingService.findAllEnvironmentalReadings(
                        pageNo, pageSize, sortBy, sortDir);

        assertEquals(environmentalReadingList.size(), result.data().size());
        assertEquals(environmentalReadingList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllEnvironmentalReadingsBySensorSystemId() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<EnvironmentalReading> environmentalReadingList =
                Collections.singletonList(environmentalReading);
        Page<EnvironmentalReading> environmentalReadingPage =
                new PageImpl<>(environmentalReadingList);

        when(environmentalReadingRepository.findAllBySensorSystemId(
                        any(UUID.class), any(Pageable.class)))
                .thenReturn(environmentalReadingPage);

        PagedResult<EnvironmentalReading> result =
                environmentalReadingService.findAllEnvironmentalReadingsBySensorSystemId(
                        UUID.randomUUID(), pageNo, pageSize, sortBy, sortDir);

        assertEquals(environmentalReadingList.size(), result.data().size());
        assertEquals(environmentalReadingList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findEnvironmentalReadingById() {
        when(environmentalReadingRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(environmentalReading));

        Optional<EnvironmentalReading> result =
                environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId);

        assertTrue(result.isPresent());
        assertEquals(environmentalReading.getId(), result.get().getId());
    }

    @Test
    void findLatestEnvironmentalReadingsByUnconvUserId() {}

    @Test
    void saveEnvironmentalReading() {

        when(environmentalReadingRepository.save(any(EnvironmentalReading.class)))
                .thenReturn(environmentalReading);

        EnvironmentalReading result =
                environmentalReadingService.saveEnvironmentalReading(environmentalReading);

        assertEquals(environmentalReading.getId(), result.getId());
    }

    @Test
    void generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading() {}

    @Test
    void parseFromCSVAndSaveEnvironmentalReading() {}

    @Test
    void deleteEnvironmentalReadingById() {
        environmentalReadingService.deleteEnvironmentalReadingById(environmentalReadingId);

        verify(environmentalReadingRepository, times(1)).deleteById(environmentalReadingId);
    }

    @Test
    void verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings() {}
}
