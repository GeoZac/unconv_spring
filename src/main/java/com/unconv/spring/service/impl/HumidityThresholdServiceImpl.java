package com.unconv.spring.service.impl;

import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.HumidityThresholdRepository;
import com.unconv.spring.service.HumidityThresholdService;
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
public class HumidityThresholdServiceImpl implements HumidityThresholdService {

    @Autowired private HumidityThresholdRepository humidityThresholdRepository;

    @Override
    public PagedResult<HumidityThreshold> findAllHumidityThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<HumidityThreshold> humidityThresholdPage =
                humidityThresholdRepository.findAll(pageable);

        return new PagedResult<>(humidityThresholdPage);
    }

    @Override
    public Optional<HumidityThreshold> findHumidityThresholdById(UUID id) {
        return humidityThresholdRepository.findById(id);
    }

    @Override
    public HumidityThreshold saveHumidityThreshold(HumidityThreshold sensorLocation) {
        return humidityThresholdRepository.save(sensorLocation);
    }

    @Override
    public void deleteHumidityThresholdById(UUID id) {
        humidityThresholdRepository.deleteById(id);
    }
}
