package com.unconv.spring.service.impl;

import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_DLTD;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.model.response.ExtremeReadingsResponse;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.projection.EnvironmentalReadingProjection;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class EnvironmentalReadingServiceImplTest {

    @Mock private EnvironmentalReadingRepository environmentalReadingRepository;

    @Mock private SensorSystemRepository sensorSystemRepository;

    @Mock private ModelMapper modelMapper;

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
    void findAllEnvironmentalReadingsInAscendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
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
    void findAllEnvironmentalReadingsInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
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
    void findAllEnvironmentalReadingsBySensorSystemIdInAscendingOrder() {
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
    void findAllEnvironmentalReadingsBySensorSystemIdInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
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
    void findLatestEnvironmentalReadingsByUnconvUserId() {
        when(environmentalReadingRepository
                        .findFirst10BySensorSystemUnconvUserIdOrderByTimestampDesc(any(UUID.class)))
                .thenReturn(List.of(environmentalReading));

        List<EnvironmentalReading> result =
                environmentalReadingService.findLatestEnvironmentalReadingsByUnconvUserId(
                        UUID.randomUUID());

        assertFalse(result.isEmpty());
        assertEquals(environmentalReading.getId(), result.get(0).getId());
    }

    @Test
    void saveEnvironmentalReading() {

        when(environmentalReadingRepository.save(any(EnvironmentalReading.class)))
                .thenReturn(environmentalReading);

        EnvironmentalReading result =
                environmentalReadingService.saveEnvironmentalReading(environmentalReading);

        assertEquals(environmentalReading.getId(), result.getId());
    }

    @Test
    void generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading() {
        // Given
        Authentication authentication = mock(Authentication.class);
        EnvironmentalReadingDTO environmentalReadingDTO = new EnvironmentalReadingDTO();
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setId(UUID.randomUUID());
        sensorSystem.setSensorStatus(SensorStatus.ACTIVE);
        sensorSystem.setDeleted(false);
        UnconvUser unconvUser = new UnconvUser();
        unconvUser.setUsername("TestUser");
        sensorSystem.setUnconvUser(unconvUser);

        environmentalReadingDTO.setSensorSystem(sensorSystem);

        EnvironmentalReading environmentalReading = new EnvironmentalReading();
        environmentalReading.setSensorSystem(sensorSystem);

        when(sensorSystemRepository.findSensorSystemById(any())).thenReturn(sensorSystem);
        when(authentication.getName()).thenReturn("TestUser");
        when(modelMapper.map(any(EnvironmentalReadingDTO.class), eq(EnvironmentalReading.class)))
                .thenReturn(environmentalReading);
        when(environmentalReadingRepository.save(any(EnvironmentalReading.class)))
                .thenReturn(environmentalReading);
        when(modelMapper.map(any(EnvironmentalReading.class), eq(EnvironmentalReadingDTO.class)))
                .thenReturn(environmentalReadingDTO);

        // When
        ResponseEntity<MessageResponse<EnvironmentalReadingDTO>> response =
                environmentalReadingService
                        .generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                                environmentalReadingDTO, authentication);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ENVT_RECORD_ACCEPTED, Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldReturnUnauthorizedWhenSavingEnvironmentalReadingWithUserMismatched() {
        // Given
        Authentication authentication = mock(Authentication.class);
        EnvironmentalReadingDTO environmentalReadingDTO = new EnvironmentalReadingDTO();
        SensorSystem sensorSystem = new SensorSystem();
        UnconvUser unconvUser = new UnconvUser();
        unconvUser.setUsername("expectedUser");
        sensorSystem.setUnconvUser(unconvUser);

        environmentalReadingDTO.setSensorSystem(sensorSystem);

        when(authentication.getName()).thenReturn("wrongUser");
        when(sensorSystemRepository.findSensorSystemById(any())).thenReturn(sensorSystem);

        // When
        ResponseEntity<MessageResponse<EnvironmentalReadingDTO>> response =
                environmentalReadingService
                        .generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                                environmentalReadingDTO, authentication);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ENVT_RECORD_REJ_USER, response.getBody().message());
    }

    @Test
    void shouldRejectWhenSavingEnvironmentalReadingWithSensorSystemDeleted() {
        // Given
        Authentication authentication = mock(Authentication.class);
        EnvironmentalReadingDTO environmentalReadingDTO = new EnvironmentalReadingDTO();
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setId(UUID.randomUUID());
        sensorSystem.setSensorStatus(SensorStatus.ACTIVE);
        sensorSystem.setDeleted(true);
        UnconvUser unconvUser = new UnconvUser();
        unconvUser.setUsername("TestUser");
        sensorSystem.setUnconvUser(unconvUser);

        environmentalReadingDTO.setSensorSystem(sensorSystem);

        EnvironmentalReading environmentalReading = new EnvironmentalReading();
        environmentalReading.setSensorSystem(sensorSystem);

        when(sensorSystemRepository.findSensorSystemById(any())).thenReturn(sensorSystem);
        when(authentication.getName()).thenReturn("TestUser");

        ResponseEntity<MessageResponse<EnvironmentalReadingDTO>> response =
                environmentalReadingService
                        .generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                                environmentalReadingDTO, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ENVT_RECORD_REJ_DLTD, Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void parseFromCSVAndSaveEnvironmentalReading() {}

    @Test
    void deleteEnvironmentalReadingById() {
        environmentalReadingService.deleteEnvironmentalReadingById(environmentalReadingId);

        verify(environmentalReadingRepository, times(1)).deleteById(environmentalReadingId);
    }

    @Test
    void verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings() {}

    @Test
    void getExtremeReadingsResponseBySensorSystemId() {
        UUID sensorSystemId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        MockEnvironmentalReadingProjection maxTemp =
                new MockEnvironmentalReadingProjection(64.0, 34.0, now);
        MockEnvironmentalReadingProjection minTemp =
                new MockEnvironmentalReadingProjection(12.0, 30.0, now.minusDays(1));
        MockEnvironmentalReadingProjection maxHumidity =
                new MockEnvironmentalReadingProjection(50.0, 80.0, now.minusHours(2));
        MockEnvironmentalReadingProjection minHumidity =
                new MockEnvironmentalReadingProjection(25.0, 20.0, now.minusHours(3));

        when(environmentalReadingRepository.findFirstBySensorSystemIdOrderByTemperatureDesc(
                        sensorSystemId))
                .thenReturn(maxTemp);
        when(environmentalReadingRepository.findFirstBySensorSystemIdOrderByTemperatureAsc(
                        sensorSystemId))
                .thenReturn(minTemp);
        when(environmentalReadingRepository.findFirstBySensorSystemIdOrderByHumidityDesc(
                        sensorSystemId))
                .thenReturn(maxHumidity);
        when(environmentalReadingRepository.findFirstBySensorSystemIdOrderByHumidityAsc(
                        sensorSystemId))
                .thenReturn(minHumidity);

        ExtremeReadingsResponse response =
                environmentalReadingService.getExtremeReadingsResponseBySensorSystemId(
                        sensorSystemId);

        assertNotNull(response);
        assertEquals(64.0, response.getMaxTemperature().getTemperature());
        assertEquals(12.0, response.getMinTemperature().getTemperature());
        assertEquals(80.0, response.getMaxHumidity().getHumidity());
        assertEquals(20.0, response.getMinHumidity().getHumidity());
    }

    public record MockEnvironmentalReadingProjection(
            double temperature, double humidity, OffsetDateTime timestamp)
            implements EnvironmentalReadingProjection {
        @Override
        public double getTemperature() {
            return temperature;
        }

        @Override
        public double getHumidity() {
            return humidity;
        }

        @Override
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }
    }
}
