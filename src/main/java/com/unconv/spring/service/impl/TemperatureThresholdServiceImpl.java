package com.unconv.spring.service.impl;

import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.TemperatureThresholdRepository;
import com.unconv.spring.service.TemperatureThresholdService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TemperatureThresholdServiceImpl implements TemperatureThresholdService {

    @Autowired private TemperatureThresholdRepository temperatureThresholdRepository;

    @Override
    public PagedResult<TemperatureThreshold> findAllTemperatureThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<TemperatureThreshold> temperatureThresholdPage =
                temperatureThresholdRepository.findAll(pageable);

        return new PagedResult<>(temperatureThresholdPage);
    }

    @Override
    public Optional<TemperatureThreshold> findTemperatureThresholdById(UUID id) {
        return temperatureThresholdRepository.findById(id);
    }

    @Override
    public TemperatureThreshold saveTemperatureThreshold(
            TemperatureThreshold temperatureThreshold) {
        return temperatureThresholdRepository.save(temperatureThreshold);
    }

    @Override
    public void deleteTemperatureThresholdById(UUID id) {
        temperatureThresholdRepository.deleteById(id);
    }
}
