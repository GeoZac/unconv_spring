package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.Heater;
import com.unconv.spring.persistence.HeaterRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeaterServiceImplTest {

    @Mock private HeaterRepository heaterRepository;

    @InjectMocks private HeaterServiceImpl heaterService;

    private Heater heater;
    private Long heaterId;

    @BeforeEach
    void setUp() {
        heaterId = 1L;
        heater = new Heater();
        heater.setId(heaterId);
    }

    @Test
    void findAllHeaters() {

        List<Heater> heaterList = Collections.singletonList(heater);

        when(heaterRepository.findAll()).thenReturn(heaterList);

        List<Heater> result = heaterService.findAllHeaters();

        assertEquals(heaterList.size(), result.size());
        assertEquals(heaterList.get(0).getId(), result.get(0).getId());
    }

    @Test
    void findHeaterById() {
        when(heaterRepository.findById(any(Long.class))).thenReturn(Optional.of(heater));

        Optional<Heater> result = heaterService.findHeaterById(heaterId);

        assertEquals(heater.getId(), result.get().getId());
    }

    @Test
    void saveHeater() {
        when(heaterRepository.save(any(Heater.class))).thenReturn(heater);

        Heater result = heaterService.saveHeater(heater);

        assertEquals(heater.getId(), result.getId());
    }

    @Test
    void deleteHeaterById() {
        heaterService.deleteHeaterById(heaterId);

        verify(heaterRepository, times(1)).deleteById(heaterId);
    }
}
