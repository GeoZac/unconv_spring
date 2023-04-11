package com.unconv.spring.service;

import com.unconv.spring.domain.Heater;
import com.unconv.spring.persistence.HeaterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HeaterService {

    @Autowired private HeaterRepository heaterRepository;

    public List<Heater> findAllHeaters() {
        return heaterRepository.findAll();
    }

    public Optional<Heater> findHeaterById(Long id) {
        return heaterRepository.findById(id);
    }

    public Heater saveHeater(Heater heater) {
        return heaterRepository.save(heater);
    }

    public void deleteHeaterById(Long id) {
        heaterRepository.deleteById(id);
    }
}
