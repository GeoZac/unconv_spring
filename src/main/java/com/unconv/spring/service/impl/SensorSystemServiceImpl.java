package com.unconv.spring.service.impl;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.dto.base.BaseEnvironmentalReadingDTO;
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

        Page<SensorSystemDTO> sensorSystemDTOPage =
                new PageImpl<>(populateSensorSystemDTOFromSensorSystemPage(sensorSystemsPage));

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
                sensorSystemRepository.findByUnconvUserIdAndDeletedFalse(unconvUserId, pageable);

        Page<SensorSystemDTO> sensorSystemDTOPage =
                new PageImpl<>(populateSensorSystemDTOFromSensorSystemPage(sensorSystemsPage));

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
            EnvironmentalReading environmentalReading =
                    environmentalReadingRepository.findFirstBySensorSystemIdOrderByTimestampDesc(
                            id);
            if (environmentalReading != null) {
                sensorSystemDTO.setLatestReading(
                        modelMapper.map(environmentalReading, BaseEnvironmentalReadingDTO.class));
            }
            return Optional.of(sensorSystemDTO);
        }
    }

    @Override
    public SensorSystem saveSensorSystem(SensorSystem sensorSystem) {
        return sensorSystemRepository.save(sensorSystem);
    }

    @Override
    public boolean deleteSensorSystemById(UUID id) {
        if (environmentalReadingRepository.countBySensorSystemId(id) != 0) {
            SensorSystem sensorSystem = sensorSystemRepository.findSensorSystemById(id);
            sensorSystem.setDeleted(true);
            sensorSystemRepository.save(sensorSystem);
            return false;
        } else {
            sensorSystemRepository.deleteById(id);
            return true;
        }
    }

    @Override
    public List<SensorSystem> findAllSensorSystemsBySensorName(String sensorName) {
        return sensorSystemRepository
                .findDistinctBySensorNameContainingIgnoreCaseOrderBySensorNameAsc(sensorName);
    }

    private List<SensorSystemDTO> populateSensorSystemDTOFromSensorSystemPage(
            Page<SensorSystem> sensorSystemsPage) {
        List<SensorSystem> sensorSystems = sensorSystemsPage.getContent();
        List<SensorSystemDTO> sensorSystemDTOs = new ArrayList<>();
        for (SensorSystem sensorSystem : sensorSystems) {
            SensorSystemDTO sensorSystemDTO =
                    mapSensorSystemEntityToDTOAndPopulateExtraFields(sensorSystem);
            sensorSystemDTOs.add(sensorSystemDTO);
        }

        return sensorSystemDTOs;
    }

    private SensorSystemDTO mapSensorSystemEntityToDTOAndPopulateExtraFields(
            SensorSystem sensorSystem) {
        SensorSystemDTO sensorSystemDTO = modelMapper.map(sensorSystem, SensorSystemDTO.class);
        sensorSystemDTO.setReadingCount(
                environmentalReadingRepository.countBySensorSystemId(sensorSystem.getId()));
        EnvironmentalReading environmentalReading =
                environmentalReadingRepository.findFirstBySensorSystemIdOrderByTimestampDesc(
                        sensorSystem.getId());

        if (environmentalReading != null) {
            sensorSystemDTO.setLatestReading(
                    modelMapper.map(environmentalReading, BaseEnvironmentalReadingDTO.class));
        }
        return sensorSystemDTO;
    }
}
