package com.unconv.spring.service.impl;

import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_USER;
import static com.unconv.spring.consts.MessageConstants.SENS_RECORD_REJ_USER;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.persistence.UnconvUserRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class SensorSystemServiceImplTest {

    @Spy private ModelMapper modelMapper;

    @Mock private SensorSystemRepository sensorSystemRepository;

    @Mock private EnvironmentalReadingRepository environmentalReadingRepository;

    @Mock private UnconvUserRepository unconvUserRepository;

    @InjectMocks private SensorSystemServiceImpl sensorSystemService;

    private SensorSystem sensorSystem;
    private UUID sensorSystemId;

    @BeforeEach
    void setUp() {
        sensorSystemId = UUID.randomUUID();
        sensorSystem = new SensorSystem();
        sensorSystem.setId(sensorSystemId);
    }

    @Test
    void findAllSensorSystemsInAscendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<SensorSystem> sensorSystemList = Collections.singletonList(sensorSystem);
        Page<SensorSystem> sensorLocationPage = new PageImpl<>(sensorSystemList);

        when(sensorSystemRepository.findAll(any(Pageable.class))).thenReturn(sensorLocationPage);

        PagedResult<SensorSystemDTO> result =
                sensorSystemService.findAllSensorSystems(pageNo, pageSize, sortBy, sortDir);

        assertEquals(sensorSystemList.size(), result.data().size());
        assertEquals(sensorSystemList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllSensorSystemsInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
        List<SensorSystem> sensorSystemList = Collections.singletonList(sensorSystem);
        Page<SensorSystem> sensorLocationPage = new PageImpl<>(sensorSystemList);

        when(sensorSystemRepository.findAll(any(Pageable.class))).thenReturn(sensorLocationPage);

        PagedResult<SensorSystemDTO> result =
                sensorSystemService.findAllSensorSystems(pageNo, pageSize, sortBy, sortDir);

        assertEquals(sensorSystemList.size(), result.data().size());
        assertEquals(sensorSystemList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllSensorSystemsByUnconvUserIdInAscendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<SensorSystem> sensorSystemList = Collections.singletonList(sensorSystem);
        Page<SensorSystem> sensorLocationPage = new PageImpl<>(sensorSystemList);

        when(sensorSystemRepository.findByUnconvUserIdAndDeletedFalse(
                        any(UUID.class), any(Pageable.class)))
                .thenReturn(sensorLocationPage);

        PagedResult<SensorSystemDTO> result =
                sensorSystemService.findAllSensorSystemsByUnconvUserId(
                        UUID.randomUUID(), pageNo, pageSize, sortBy, sortDir);

        assertEquals(sensorSystemList.size(), result.data().size());
        assertEquals(sensorSystemList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllSensorSystemsByUnconvUserIdInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
        List<SensorSystem> sensorSystemList = Collections.singletonList(sensorSystem);
        Page<SensorSystem> sensorLocationPage = new PageImpl<>(sensorSystemList);

        when(sensorSystemRepository.findByUnconvUserIdAndDeletedFalse(
                        any(UUID.class), any(Pageable.class)))
                .thenReturn(sensorLocationPage);

        PagedResult<SensorSystemDTO> result =
                sensorSystemService.findAllSensorSystemsByUnconvUserId(
                        UUID.randomUUID(), pageNo, pageSize, sortBy, sortDir);

        assertEquals(sensorSystemList.size(), result.data().size());
        assertEquals(sensorSystemList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findSensorSystemById() {
        when(sensorSystemRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sensorSystem));

        Optional<SensorSystem> result = sensorSystemService.findSensorSystemById(sensorSystemId);

        assertTrue(result.isPresent());
        assertEquals(sensorSystem.getId(), result.get().getId());
    }

    @Test
    void findSensorSystemDTOById() {
        when(sensorSystemRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sensorSystem));

        Optional<SensorSystemDTO> result =
                sensorSystemService.findSensorSystemDTOById(sensorSystemId);

        assertTrue(result.isPresent());
        assertEquals(sensorSystem.getId(), result.get().getId());
    }

    @Test
    void findSensorSystemDTOByIdWithReadingsPresent() {
        when(sensorSystemRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sensorSystem));
        when(environmentalReadingRepository.findFirstBySensorSystemIdOrderByTimestampDesc(
                        any(UUID.class)))
                .thenReturn(
                        new EnvironmentalReading(
                                UUID.randomUUID(),
                                13L,
                                75L,
                                OffsetDateTime.of(
                                        LocalDateTime.of(2023, 1, 17, 17, 39), ZoneOffset.UTC),
                                sensorSystem));

        Optional<SensorSystemDTO> result =
                sensorSystemService.findSensorSystemDTOById(sensorSystemId);

        assertTrue(result.isPresent());
        assertEquals(sensorSystem.getId(), result.get().getId());
        assertNotEquals(null, result.get().getLatestReading());
    }

    @Test
    void isActiveSensorSystemWhenSensorSystemDeleted() {
        sensorSystem.setDeleted(true);

        boolean result = sensorSystemService.isActiveSensorSystem(sensorSystem);

        assertFalse(result);
    }

    @Test
    void isActiveSensorSystemWhenSensorSystemInactive() {
        sensorSystem.setSensorStatus(SensorStatus.INACTIVE);

        boolean result = sensorSystemService.isActiveSensorSystem(sensorSystem);

        assertFalse(result);
    }

    @Test
    void isActiveSensorSystemWhenSensorSystemNotDeleted() {
        sensorSystem.setDeleted(false);

        boolean result = sensorSystemService.isActiveSensorSystem(sensorSystem);

        assertTrue(result);
    }

    @Test
    void isActiveSensorSystemWhenSensorSystemActive() {
        sensorSystem.setSensorStatus(SensorStatus.ACTIVE);

        boolean result = sensorSystemService.isActiveSensorSystem(sensorSystem);

        assertTrue(result);
    }

    @Test
    void saveSensorSystem() {
        when(sensorSystemRepository.save(any(SensorSystem.class))).thenReturn(sensorSystem);

        SensorSystem result = sensorSystemService.saveSensorSystem(sensorSystem);

        assertEquals(sensorSystem.getId(), result.getId());
    }

    @Test
    void validateUnconvUserAndSaveSensorSystem() {

        Authentication authentication = mock(Authentication.class);

        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser = new UnconvUser();
        unconvUser.setId(unconvUserId);
        unconvUser.setUsername("TestUser");

        SensorSystemDTO sensorSystemDTO = new SensorSystemDTO();
        sensorSystemDTO.setUnconvUser(unconvUser);

        SensorSystem resSensorSystem = new SensorSystem();
        resSensorSystem.setId(UUID.randomUUID());
        resSensorSystem.setUnconvUser(unconvUser);

        when(unconvUserRepository.findById(unconvUserId)).thenReturn(Optional.of(unconvUser));
        when(authentication.getName()).thenReturn("TestUser");

        when(sensorSystemRepository.save(any(SensorSystem.class))).thenReturn(resSensorSystem);

        ResponseEntity<MessageResponse<SensorSystemDTO>> result =
                sensorSystemService.validateUnconvUserAndSaveSensorSystem(
                        sensorSystemDTO, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(ENVT_RECORD_ACCEPTED, Objects.requireNonNull(result.getBody()).message());
    }

    @Test
    void shouldReturnUnauthorizedWhenSavingSensorSystemWithUnknownUser() {
        Authentication authentication = mock(Authentication.class);

        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser = new UnconvUser();
        unconvUser.setId(unconvUserId);
        unconvUser.setUsername("TestUser");

        SensorSystemDTO sensorSystemDTO = new SensorSystemDTO();
        sensorSystemDTO.setUnconvUser(unconvUser);

        SensorSystem resSensorSystem = new SensorSystem();
        resSensorSystem.setId(UUID.randomUUID());
        resSensorSystem.setUnconvUser(unconvUser);

        when(unconvUserRepository.findById(unconvUserId)).thenReturn(Optional.empty());

        ResponseEntity<MessageResponse<SensorSystemDTO>> result =
                sensorSystemService.validateUnconvUserAndSaveSensorSystem(
                        sensorSystemDTO, authentication);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals(SENS_RECORD_REJ_USER, Objects.requireNonNull(result.getBody()).message());
    }

    @Test
    void shouldReturnUnauthorizedWhenSavingSensorSystemWithUserMismatched() {
        Authentication authentication = mock(Authentication.class);

        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser = new UnconvUser();
        unconvUser.setId(unconvUserId);
        unconvUser.setUsername("TestUser");

        SensorSystemDTO sensorSystemDTO = new SensorSystemDTO();
        sensorSystemDTO.setUnconvUser(unconvUser);

        SensorSystem resSensorSystem = new SensorSystem();
        resSensorSystem.setId(UUID.randomUUID());
        resSensorSystem.setUnconvUser(unconvUser);

        when(unconvUserRepository.findById(unconvUserId)).thenReturn(Optional.of(unconvUser));
        when(authentication.getName()).thenReturn("TestUser1");

        ResponseEntity<MessageResponse<SensorSystemDTO>> result =
                sensorSystemService.validateUnconvUserAndSaveSensorSystem(
                        sensorSystemDTO, authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals(ENVT_RECORD_REJ_USER, Objects.requireNonNull(result.getBody()).message());
    }

    @Test
    void deleteSensorSystemById() {
        boolean result = sensorSystemService.deleteSensorSystemById(sensorSystemId);

        assertTrue(result);
        verify(sensorSystemRepository, times(1)).deleteById(sensorSystemId);
    }

    @Test
    void deleteSensorSystemByIdWithReadingsPresent() {
        when(environmentalReadingRepository.countBySensorSystemId(any(UUID.class))).thenReturn(1L);

        when(sensorSystemRepository.findSensorSystemById(any(UUID.class))).thenReturn(sensorSystem);

        boolean result = sensorSystemService.deleteSensorSystemById(sensorSystemId);
        assertFalse(result);
        verify(sensorSystemRepository, times(0)).deleteById(sensorSystemId);
    }

    @Test
    void findAllSensorSystemsBySensorName() {
        String sensorName = "Temp";
        UUID unconvUserId = UUID.randomUUID();

        UnconvUser unconvUser =
                new UnconvUser(
                        unconvUserId, "Specific UnconvUser", "unconvuser@email.com", "password");

        List<SensorSystem> sensorSystems =
                Instancio.ofList(SensorSystem.class)
                        .size(10)
                        .ignore(field(SensorSystem::getSensorLocation))
                        .supply(field(SensorSystem::getUnconvUser), () -> unconvUser)
                        .generate(
                                field(SensorSystem.class, "sensorName"),
                                gen ->
                                        gen.ints()
                                                .range(0, 10)
                                                .as(num -> sensorName + num.toString()))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .create();

        when(sensorSystemRepository
                        .findDistinctBySensorNameContainingIgnoreCaseOrderBySensorNameAsc(
                                sensorName))
                .thenReturn(sensorSystems);

        List<SensorSystem> actualList =
                sensorSystemService.findAllSensorSystemsBySensorName(sensorName);

        assertEquals(sensorSystems, actualList);
        verify(sensorSystemRepository, times(1))
                .findDistinctBySensorNameContainingIgnoreCaseOrderBySensorNameAsc(sensorName);
    }

    @Test
    void findRecentStatsBySensorSystemId() {
        Map<Integer, Long> result =
                sensorSystemService.findRecentStatsBySensorSystemId(sensorSystemId);
        assertEquals(5, result.size());
    }

    @Test
    void findAllBySensorSystemsBySensorNameAndUnconvUserId() {
        String sensorName = "Temp";
        UUID unconvUserId = UUID.randomUUID();

        UnconvUser unconvUser =
                new UnconvUser(
                        unconvUserId, "Specific UnconvUser", "unconvuser@email.com", "password");

        List<SensorSystem> sensorSystems =
                Instancio.ofList(SensorSystem.class)
                        .size(4)
                        .ignore(field(SensorSystem::getSensorLocation))
                        .supply(field(SensorSystem::getUnconvUser), () -> unconvUser)
                        .generate(
                                field(SensorSystem.class, "sensorName"),
                                gen ->
                                        gen.ints()
                                                .range(0, 10)
                                                .as(num -> sensorName + num.toString()))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .create();

        when(sensorSystemRepository
                        .findDistinctBySensorNameContainsIgnoreCaseAndUnconvUserIdOrderBySensorNameAsc(
                                sensorName, unconvUserId))
                .thenReturn(sensorSystems);

        List<SensorSystem> actualList =
                sensorSystemService.findAllBySensorSystemsBySensorNameAndUnconvUserId(
                        sensorName, unconvUserId);

        assertEquals(sensorSystems, actualList);
        verify(sensorSystemRepository, times(1))
                .findDistinctBySensorNameContainsIgnoreCaseAndUnconvUserIdOrderBySensorNameAsc(
                        sensorName, unconvUserId);
    }
}
