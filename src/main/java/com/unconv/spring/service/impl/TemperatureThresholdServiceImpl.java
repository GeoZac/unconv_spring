package com.unconv.spring.service.impl;

import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.domain.shared.Threshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.ThresholdRepository;
import com.unconv.spring.service.TemperatureThresholdService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TemperatureThresholdServiceImpl implements TemperatureThresholdService {

    @Autowired private ThresholdRepository temperatureThresholdRepository;

    @Override
    public PagedResult<TemperatureThreshold> findAllTemperatureThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Threshold> thresholdPage = temperatureThresholdRepository.findAll(pageable);
        List<TemperatureThreshold> temperatureThresholdList = new ArrayList<>();

        for (Threshold threshold : thresholdPage) {
            if (threshold instanceof TemperatureThreshold) {
                temperatureThresholdList.add((TemperatureThreshold) threshold);
            }
        }

        Page<TemperatureThreshold> temperatureThresholdPage =
                new PageImpl<>(
                        temperatureThresholdList,
                        thresholdPage.getPageable(),
                        thresholdPage.getTotalElements());

        return new PagedResult<>(temperatureThresholdPage);
    }

    @Override
    public Optional<TemperatureThreshold> findTemperatureThresholdById(UUID id) {
        Optional<Threshold> optionalThreshold = temperatureThresholdRepository.findById(id);

        if (optionalThreshold.isPresent()) {
            Threshold threshold = optionalThreshold.get();
            if (threshold instanceof TemperatureThreshold) {
                return Optional.of((TemperatureThreshold) threshold);
            }
        }

        return Optional.empty();
    }

    @Override
    public TemperatureThreshold saveTemperatureThreshold(TemperatureThreshold sensorLocation) {
        return temperatureThresholdRepository.save(sensorLocation);
    }

    @Override
    public void deleteTemperatureThresholdById(UUID id) {
        temperatureThresholdRepository.deleteById(id);
    }
}
