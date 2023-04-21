package com.unconv.spring.service;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.dto.TenMinuteTemperature;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EnvironmentalReadingService {

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    public PagedResult<EnvironmentalReading> findAllEnvironmentalReadings(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<EnvironmentalReading> environmentalReadingsPage =
                environmentalReadingRepository.findAll(pageable);

        return new PagedResult<>(environmentalReadingsPage);
    }

    public Optional<EnvironmentalReading> findEnvironmentalReadingById(UUID id) {
        return environmentalReadingRepository.findById(id);
    }

    public EnvironmentalReading saveEnvironmentalReading(
            EnvironmentalReading environmentalReading) {
        return environmentalReadingRepository.save(environmentalReading);
    }

    public void deleteEnvironmentalReadingById(UUID id) {
        environmentalReadingRepository.deleteById(id);
    }

    public List<TenMinuteTemperature> getTenMinuteTemperature() {
        List<Object[]> results = environmentalReadingRepository.findTemperatureBy10MinuteInterval();
        List<TenMinuteTemperature> tenMinuteTemperatures = new ArrayList<>();

        for (Object[] row : results) {
            LocalDateTime interval =
                    LocalDateTime.parse(
                            (String) row[0], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Double temperature = (Double) row[1];
            tenMinuteTemperatures.add(new TenMinuteTemperature(interval, temperature));
        }

        return tenMinuteTemperatures;
    }
}
