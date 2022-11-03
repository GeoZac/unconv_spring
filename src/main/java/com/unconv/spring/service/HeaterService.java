package com.unconv.spring.service;

import com.unconv.spring.domain.Heater;
import java.util.List;
import java.util.Optional;

public interface HeaterService {
    public List<Heater> findAllHeaters();

    public Optional<Heater> findHeaterById(Long id);

    public Heater saveHeater(Heater heater);

    public void deleteHeaterById(Long id);
}
