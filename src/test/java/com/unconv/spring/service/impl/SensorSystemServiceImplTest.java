package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import java.util.Collections;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    void findAllSensorSystems() {
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
    void validateUnconvUserAndSaveSensorSystem() {}

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
    void findAllSensorSystemsBySensorName() {}

    @Test
    void findRecentStatsBySensorSystemId() {
        Map<Integer, Long> result =
                sensorSystemService.findRecentStatsBySensorSystemId(sensorSystemId);
        assertEquals(5, result.size());
    }

    @Test
    void findAllBySensorSystemsBySensorNameAndUnconvUserId() {}
}
