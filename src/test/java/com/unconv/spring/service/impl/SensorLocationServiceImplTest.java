package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorLocationRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SensorLocationServiceImplTest {

    @Mock private SensorLocationRepository sensorLocationRepository;

    @Mock private SensorSystemRepository sensorSystemRepository;

    @InjectMocks private SensorLocationServiceImpl sensorLocationService;

    private SensorLocation sensorLocation;
    private UUID sensorLocationId;

    @BeforeEach
    void setUp() {
        sensorLocationId = UUID.randomUUID();
        sensorLocation = new SensorLocation();
        sensorLocation.setId(sensorLocationId);
    }

    @Test
    void findAllSensorLocations() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<SensorLocation> sensorLocationList = Collections.singletonList(sensorLocation);
        Page<SensorLocation> sensorLocationPage = new PageImpl<>(sensorLocationList);

        when(sensorLocationRepository.findAll(any(Pageable.class))).thenReturn(sensorLocationPage);

        PagedResult<SensorLocation> result =
                sensorLocationService.findAllSensorLocations(pageNo, pageSize, sortBy, sortDir);

        assertEquals(sensorLocationList.size(), result.data().size());
        assertEquals(sensorLocationList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllSensorLocationsByUnconvUserId() {
        UUID unconvUserId = UUID.randomUUID();
        List<SensorLocation> sensorLocationList = Collections.singletonList(sensorLocation);

        when(sensorSystemRepository.findDistinctByUnconvUserId(any(UUID.class)))
                .thenReturn(sensorLocationList);

        List<SensorLocation> result =
                sensorLocationService.findAllSensorLocationsByUnconvUserId(unconvUserId);

        assertEquals(sensorLocationList.size(), result.size());
        assertEquals(sensorLocationList.get(0).getId(), result.get(0).getId());
    }

    @Test
    void findSensorLocationById() {
        when(sensorLocationRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sensorLocation));

        Optional<SensorLocation> result =
                sensorLocationService.findSensorLocationById(sensorLocationId);

        assertEquals(sensorLocation.getId(), result.get().getId());
    }

    @Test
    void saveSensorLocation() {
        when(sensorLocationRepository.save(any(SensorLocation.class))).thenReturn(sensorLocation);

        SensorLocation result = sensorLocationService.saveSensorLocation(sensorLocation);

        assertEquals(sensorLocation.getId(), result.getId());
    }

    @Test
    void deleteSensorLocationById() {
        sensorLocationService.deleteSensorLocationById(sensorLocationId);

        verify(sensorLocationRepository, times(1)).deleteById(sensorLocationId);
    }
}
