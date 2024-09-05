package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.TemperatureThresholdRepository;
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
class TemperatureThresholdServiceImplTest {

    @Mock private TemperatureThresholdRepository temperatureThresholdRepository;

    @InjectMocks private TemperatureThresholdServiceImpl temperatureThresholdService;

    private TemperatureThreshold temperatureThreshold;
    private UUID temperatureThresholdId;

    @BeforeEach
    void setUp() {
        temperatureThresholdId = UUID.randomUUID();
        temperatureThreshold = new TemperatureThreshold();
        temperatureThreshold.setId(temperatureThresholdId);
    }

    @Test
    void findAllTemperatureThresholdsInAscendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<TemperatureThreshold> temperatureThresholdList =
                Collections.singletonList(temperatureThreshold);
        Page<TemperatureThreshold> temperatureThresholdPage =
                new PageImpl<>(temperatureThresholdList);

        when(temperatureThresholdRepository.findAll(any(Pageable.class)))
                .thenReturn(temperatureThresholdPage);

        PagedResult<TemperatureThreshold> result =
                temperatureThresholdService.findAllTemperatureThresholds(
                        pageNo, pageSize, sortBy, sortDir);

        assertEquals(temperatureThresholdList.size(), result.data().size());
        assertEquals(temperatureThresholdList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findTemperatureThresholdById() {
        when(temperatureThresholdRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(temperatureThreshold));

        Optional<TemperatureThreshold> result =
                temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId);

        assertTrue(result.isPresent());
        assertEquals(temperatureThreshold.getId(), result.get().getId());
    }

    @Test
    void saveTemperatureThreshold() {
        when(temperatureThresholdRepository.save(any(TemperatureThreshold.class)))
                .thenReturn(temperatureThreshold);

        TemperatureThreshold result =
                temperatureThresholdService.saveTemperatureThreshold(temperatureThreshold);

        assertEquals(temperatureThreshold.getId(), result.getId());
    }

    @Test
    void deleteTemperatureThresholdById() {
        temperatureThresholdService.deleteTemperatureThresholdById(temperatureThresholdId);

        verify(temperatureThresholdRepository, times(1)).deleteById(temperatureThresholdId);
    }
}
