package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.persistence.SensorSystemRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensorSystemServiceImplTest {

    @Mock private SensorSystemRepository sensorSystemRepository;

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
    void findSensorSystemDTOById() {}

    @Test
    void isActiveSensorSystem() {}

    @Test
    void saveSensorSystem() {}

    @Test
    void validateUnconvUserAndSaveSensorSystem() {}

    @Test
    void deleteSensorSystemById() {}

    @Test
    void findAllSensorSystemsBySensorName() {}

    @Test
    void findRecentStatsBySensorSystemId() {}

    @Test
    void findAllBySensorSystemsBySensorNameAndUnconvUserId() {}
}
