package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class SensorSystemServiceImplTest {

    @Spy private ModelMapper modelMapper;

    @Mock private SensorSystemRepository sensorSystemRepository;

    @Mock private EnvironmentalReadingRepository environmentalReadingRepository;

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
    void findAllSensorSystems() {}

    @Test
    void findAllSensorSystemsByUnconvUserId() {}

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
    void isActiveSensorSystemWhenSensorSystemDeleted() {
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setDeleted(true);

        boolean result = sensorSystemService.isActiveSensorSystem(sensorSystem);

        assertFalse(result);
    }

    @Test
    void isActiveSensorSystemWhenSensorSystemInactive() {
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setSensorStatus(SensorStatus.INACTIVE);

        boolean result = sensorSystemService.isActiveSensorSystem(sensorSystem);

        assertFalse(result);
    }

    @Test
    void isActiveSensorSystemWhenSensorSystemNotDeleted() {
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setDeleted(false);

        boolean result = sensorSystemService.isActiveSensorSystem(sensorSystem);

        assertTrue(result);
    }

    @Test
    void isActiveSensorSystemWhenSensorSystemActive() {
        SensorSystem sensorSystem = new SensorSystem();
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
    void validateUnconvUserAndSaveSensorSystem() {}

    @Test
    void deleteSensorSystemById() {
        sensorSystemService.deleteSensorSystemById(sensorSystemId);

        verify(sensorSystemRepository, times(1)).deleteById(sensorSystemId);
    }

    @Test
    void deleteSensorSystemByIdWithReadingsPresent() {
        when(environmentalReadingRepository.countBySensorSystemId(any(UUID.class))).thenReturn(1L);

        when(sensorSystemRepository.findSensorSystemById(any(UUID.class))).thenReturn(sensorSystem);

        sensorSystemService.deleteSensorSystemById(sensorSystemId);
        verify(sensorSystemRepository, times(0)).deleteById(sensorSystemId);
    }

    @Test
    void findAllSensorSystemsBySensorName() {}

    @Test
    void findRecentStatsBySensorSystemId() {
        Map<Integer, Long> result =
                sensorSystemService.findRecentStatsBySensorSystemId(sensorSystemId);
        assertEquals(result.size(), 5);
    }

    @Test
    void findAllBySensorSystemsBySensorNameAndUnconvUserId() {}
}
