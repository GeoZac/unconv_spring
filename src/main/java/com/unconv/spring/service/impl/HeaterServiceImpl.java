package com.unconv.spring.service.impl;

import com.unconv.spring.domain.Heater;
import com.unconv.spring.persistence.HeaterRepository;
import com.unconv.spring.service.HeaterService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HeaterServiceImpl implements HeaterService {

    private final HeaterRepository heaterRepository;

    @Autowired
    public HeaterServiceImpl(HeaterRepository heaterRepository) {
        this.heaterRepository = heaterRepository;
    }

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
