package com.unconv.spring.service;

import com.unconv.spring.domain.Heater;
import java.util.List;
import java.util.Optional;

public interface HeaterService {
    List<Heater> findAllHeaters();

    Optional<Heater> findHeaterById(Long id);

    Heater saveHeater(Heater heater);

    void deleteHeaterById(Long id);
}
