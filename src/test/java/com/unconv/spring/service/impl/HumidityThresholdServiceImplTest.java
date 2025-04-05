package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.HumidityThresholdRepository;
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
class HumidityThresholdServiceImplTest {

    @Mock private HumidityThresholdRepository humidityThresholdRepository;

    @InjectMocks private HumidityThresholdServiceImpl humidityThresholdService;

    private HumidityThreshold humidityThreshold;
    private UUID humidityThresholdId;

    @BeforeEach
    void setUp() {
        humidityThresholdId = UUID.randomUUID();
        humidityThreshold = new HumidityThreshold();
        humidityThreshold.setId(humidityThresholdId);
    }

    @Test
    void findAllHumidityThresholdsInAscendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<HumidityThreshold> humidityThresholdList =
                Collections.singletonList(humidityThreshold);
        Page<HumidityThreshold> humidityThresholdPage = new PageImpl<>(humidityThresholdList);

        when(humidityThresholdRepository.findAll(any(Pageable.class)))
                .thenReturn(humidityThresholdPage);

        PagedResult<HumidityThreshold> result =
                humidityThresholdService.findAllHumidityThresholds(
                        pageNo, pageSize, sortBy, sortDir);

        assertEquals(humidityThresholdList.size(), result.data().size());
        assertEquals(humidityThresholdList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllHumidityThresholdsInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
        List<HumidityThreshold> humidityThresholdList =
                Collections.singletonList(humidityThreshold);
        Page<HumidityThreshold> humidityThresholdPage = new PageImpl<>(humidityThresholdList);

        when(humidityThresholdRepository.findAll(any(Pageable.class)))
                .thenReturn(humidityThresholdPage);

        PagedResult<HumidityThreshold> result =
                humidityThresholdService.findAllHumidityThresholds(
                        pageNo, pageSize, sortBy, sortDir);

        assertEquals(humidityThresholdList.size(), result.data().size());
        assertEquals(humidityThresholdList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findHumidityThresholdById() {
        when(humidityThresholdRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(humidityThreshold));

        Optional<HumidityThreshold> result =
                humidityThresholdService.findHumidityThresholdById(humidityThresholdId);

        assertTrue(result.isPresent());
        assertEquals(humidityThreshold.getId(), result.get().getId());
    }

    @Test
    void saveHumidityThreshold() {
        when(humidityThresholdRepository.save(any(HumidityThreshold.class)))
                .thenReturn(humidityThreshold);

        HumidityThreshold result =
                humidityThresholdService.saveHumidityThreshold(humidityThreshold);

        assertEquals(humidityThreshold.getId(), result.getId());
    }

    @Test
    void deleteHumidityThresholdById() {
        humidityThresholdService.deleteHumidityThresholdById(humidityThresholdId);

        verify(humidityThresholdRepository, times(1)).deleteById(humidityThresholdId);
    }
}
