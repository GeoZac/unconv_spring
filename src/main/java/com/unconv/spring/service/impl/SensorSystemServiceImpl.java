package com.unconv.spring.service.impl;

import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.service.SensorSystemService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
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
public class SensorSystemServiceImpl implements SensorSystemService {

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    @Autowired private ModelMapper modelMapper;

    @Override
    public PagedResult<SensorSystemDTO> findAllSensorSystems(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<SensorSystem> sensorSystemsPage = sensorSystemRepository.findAll(pageable);

        List<SensorSystem> sensorSystems = sensorSystemsPage.getContent();
        List<SensorSystemDTO> sensorSystemDTOs = new ArrayList<>();

        for (SensorSystem sensorSystem : sensorSystems) {
            SensorSystemDTO sensorSystemDTO = modelMapper.map(sensorSystem, SensorSystemDTO.class);
            sensorSystemDTO.setReadingCount(
                    environmentalReadingRepository.countBySensorSystemId(sensorSystem.getId()));
            sensorSystemDTO.setLatestReading(
                    environmentalReadingRepository.findFirstBySensorSystemIdOrderByTimestampDesc(
                            sensorSystem.getId()));

            sensorSystemDTOs.add(sensorSystemDTO);
        }

        Page<SensorSystemDTO> sensorSystemDTOPage = new PageImpl<SensorSystemDTO>(sensorSystemDTOs);

        return new PagedResult<>(sensorSystemDTOPage);
    }

    @Override
    public PagedResult<SensorSystemDTO> findAllSensorSystemsByUnconvUserId(
            UUID unconvUserId, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<SensorSystem> sensorSystemsPage =
                sensorSystemRepository.findAllByUnconvUserId(unconvUserId, pageable);

        List<SensorSystem> sensorSystems = sensorSystemsPage.getContent();
        List<SensorSystemDTO> sensorSystemDTOs = new ArrayList<>();

        for (SensorSystem sensorSystem : sensorSystems) {
            SensorSystemDTO sensorSystemDTO = modelMapper.map(sensorSystem, SensorSystemDTO.class);
            sensorSystemDTO.setReadingCount(
                    environmentalReadingRepository.countBySensorSystemId(sensorSystem.getId()));
            sensorSystemDTO.setLatestReading(
                    environmentalReadingRepository.findFirstBySensorSystemIdOrderByTimestampDesc(
                            sensorSystem.getId()));

            sensorSystemDTOs.add(sensorSystemDTO);
        }

        Page<SensorSystemDTO> sensorSystemDTOPage = new PageImpl<SensorSystemDTO>(sensorSystemDTOs);

        return new PagedResult<>(sensorSystemDTOPage);
    }

    @Override
    public Optional<SensorSystem> findSensorSystemById(UUID id) {
        return sensorSystemRepository.findById(id);
    }

    @Override
    public Optional<SensorSystemDTO> findSensorSystemDTOById(UUID id) {
        Optional<SensorSystem> sensorSystem = sensorSystemRepository.findById(id);
        if (sensorSystem.isEmpty()) {
            return Optional.ofNullable(modelMapper.map(sensorSystem, SensorSystemDTO.class));
        } else {
            SensorSystemDTO sensorSystemDTO =
                    modelMapper.map(sensorSystem.get(), SensorSystemDTO.class);
            sensorSystemDTO.setReadingCount(
                    environmentalReadingRepository.countBySensorSystemId(id));
            sensorSystemDTO.setLatestReading(
                    environmentalReadingRepository.findFirstBySensorSystemIdOrderByTimestampDesc(
                            id));
            return Optional.of(sensorSystemDTO);
        }
    }

    @Override
    public SensorSystem saveSensorSystem(SensorSystem sensorSystem) {
        return sensorSystemRepository.save(sensorSystem);
    }

    @Override
    public void deleteSensorSystemById(UUID id) {
        sensorSystemRepository.deleteById(id);
    }
}
