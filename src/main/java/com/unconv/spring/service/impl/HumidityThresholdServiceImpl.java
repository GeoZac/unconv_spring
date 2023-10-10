package com.unconv.spring.service.impl;

import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.shared.Threshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.ThresholdRepository;
import com.unconv.spring.service.HumidityThresholdService;
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
public class HumidityThresholdServiceImpl implements HumidityThresholdService {

    @Autowired private ThresholdRepository humidityThresholdRepository;

    @Override
    public PagedResult<HumidityThreshold> findAllHumidityThresholds(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Threshold> thresholdPage = humidityThresholdRepository.findAll(pageable);
        List<HumidityThreshold> humidityThresholdList = new ArrayList<>();

        for (Threshold threshold : thresholdPage) {
            if (threshold instanceof HumidityThreshold) {
                humidityThresholdList.add((HumidityThreshold) threshold);
            }
        }

        Page<HumidityThreshold> humidityThresholdPage =
                new PageImpl<>(
                        humidityThresholdList,
                        thresholdPage.getPageable(),
                        thresholdPage.getTotalElements());

        return new PagedResult<>(humidityThresholdPage);
    }

    @Override
    public Optional<HumidityThreshold> findHumidityThresholdById(UUID id) {
        Optional<Threshold> optionalThreshold = humidityThresholdRepository.findById(id);

        if (optionalThreshold.isPresent()) {
            Threshold threshold = optionalThreshold.get();
            if (threshold instanceof HumidityThreshold) {
                return Optional.of((HumidityThreshold) threshold);
            }
        }

        return Optional.empty();
    }

    @Override
    public HumidityThreshold saveHumidityThreshold(HumidityThreshold humidityThreshold) {
        return humidityThresholdRepository.save(humidityThreshold);
    }

    @Override
    public void deleteHumidityThresholdById(UUID id) {
        humidityThresholdRepository.deleteById(id);
    }
}
