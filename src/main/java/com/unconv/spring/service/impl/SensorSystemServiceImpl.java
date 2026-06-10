package com.unconv.spring.service.impl;

import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_USER;
import static com.unconv.spring.consts.MessageConstants.SENS_RECORD_REJ_USER;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.dto.base.BaseEnvironmentalReadingDTO;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorLocationRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.SensorSystemService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SensorSystemServiceImpl implements SensorSystemService {

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private SensorLocationRepository sensorLocationRepository;

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    @Autowired private UnconvUserRepository unconvUserRepository;

    @Autowired private ModelMapper modelMapper;

    /**
     * Retrieves a paginated list of all SensorSystems.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of SensorSystemDTOs.
     */
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

        List<SensorSystemDTO> sensorSystemDTOs =
                populateSensorSystemDTOFromSensorSystemPage(sensorSystemsPage);

        Page<SensorSystemDTO> sensorSystemDTOPage =
                new PageImpl<>(sensorSystemDTOs, pageable, sensorSystemsPage.getTotalElements());

        return new PagedResult<>(sensorSystemDTOPage);
    }

    /**
     * Retrieves a paginated list of SensorSystems by UnconvUserId.
     *
     * @param unconvUserId The ID of the UnconvUser.
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of SensorSystemDTOs.
     */
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

        List<SensorSystemDTO> sensorSystemDTOs =
                populateSensorSystemDTOFromSensorSystemPage(sensorSystemsPage);

        Page<SensorSystemDTO> sensorSystemDTOPage =
                new PageImpl<>(sensorSystemDTOs, pageable, sensorSystemsPage.getTotalElements());

        return new PagedResult<>(sensorSystemDTOPage);
    }

    /**
     * Retrieves a SensorSystem by its ID.
     *
     * @param id The ID of the SensorSystem.
     * @return An Optional containing the SensorSystem, or empty if not found.
     */
    @Override
    public Optional<SensorSystem> findSensorSystemById(UUID id) {
        return sensorSystemRepository.findById(id);
    }

    /**
     * Retrieves a SensorSystemDTO by its ID.
     *
     * @param id The ID of the SensorSystem.
     * @return An Optional containing the SensorSystemDTO, or empty if not found.
     */
    @Override
    public Optional<SensorSystemDTO> findSensorSystemDTOById(UUID id) {
        Optional<SensorSystem> sensorSystem = sensorSystemRepository.findById(id);
        if (sensorSystem.isEmpty()) {
            return Optional.ofNullable(modelMapper.map(sensorSystem, SensorSystemDTO.class));
        } else {
            SensorSystemDTO sensorSystemDTO =
                    modelMapper.map(sensorSystem.get(), SensorSystemDTO.class);
            long readingCount = environmentalReadingRepository.countBySensorSystemId(id);
            sensorSystemDTO.setReadingCount(readingCount);
            if (readingCount != 0) {
                EnvironmentalReading environmentalReading =
                        environmentalReadingRepository
                                .findFirstBySensorSystemIdOrderByTimestampDesc(id);
                sensorSystemDTO.setLatestReading(
                        modelMapper.map(environmentalReading, BaseEnvironmentalReadingDTO.class));
            }
            return Optional.of(sensorSystemDTO);
        }
    }

    /**
     * Checks if the given SensorSystem is active.
     *
     * @param sensorSystem The SensorSystem to check.
     * @return {@code true} if the SensorSystem is active, {@code false} otherwise.
     */
    @Override
    public boolean isActiveSensorSystem(SensorSystem sensorSystem) {
        return !sensorSystem.isDeleted() && sensorSystem.getSensorStatus() != SensorStatus.INACTIVE;
    }

    /**
     * Saves a new SensorSystem.
     *
     * @param sensorSystem The SensorSystem to save.
     * @return The saved SensorSystem.
     */
    @Override
    public SensorSystem saveSensorSystem(SensorSystem sensorSystem) {
        return sensorSystemRepository.save(sensorSystem);
    }

    /**
     * Validates the UnconvUser and saves a new SensorSystem.
     *
     * @param sensorSystemDTO The SensorSystemDTO to save.
     * @param authentication The authentication object.
     * @return ResponseEntity containing a MessageResponse with the saved SensorSystemDTO.
     */
    @Override
    public ResponseEntity<MessageResponse<SensorSystemDTO>> validateUnconvUserAndSaveSensorSystem(
            SensorSystemDTO sensorSystemDTO, Authentication authentication) {

        Optional<UnconvUser> unconvUser =
                unconvUserRepository.findById(sensorSystemDTO.getUnconvUser().getId());
        if (unconvUser.isEmpty()) {
            MessageResponse<SensorSystemDTO> sensorSystemDTOMessageResponse =
                    new MessageResponse<>(sensorSystemDTO, SENS_RECORD_REJ_USER);
            return new ResponseEntity<>(sensorSystemDTOMessageResponse, HttpStatus.NOT_FOUND);
        }

        if (sensorSystemDTO.getUnconvUser().getUsername() == null) {
            sensorSystemDTO.setUnconvUser(unconvUser.get());
        }

        if (!sensorSystemDTO.getUnconvUser().getUsername().equals(authentication.getName())) {
            MessageResponse<SensorSystemDTO> sensorSystemDTOMessageResponse =
                    new MessageResponse<>(sensorSystemDTO, ENVT_RECORD_REJ_USER);
            return new ResponseEntity<>(sensorSystemDTOMessageResponse, HttpStatus.UNAUTHORIZED);
        }

        SensorLocation sensorLocation =
                resolveSensorLocationReference(sensorSystemDTO.getSensorLocation());
        if (sensorLocation != null) {
            sensorSystemDTO.setSensorLocation(sensorLocation);
        }

        SensorSystem sensorSystem =
                saveSensorSystem(modelMapper.map(sensorSystemDTO, SensorSystem.class));

        MessageResponse<SensorSystemDTO> sensorSystemDTOMessageResponse =
                new MessageResponse<>(
                        modelMapper.map(sensorSystem, SensorSystemDTO.class), ENVT_RECORD_ACCEPTED);
        return new ResponseEntity<>(sensorSystemDTOMessageResponse, HttpStatus.CREATED);
    }

    /**
     * Deletes a SensorSystem by its ID.
     *
     * @param id The ID of the SensorSystem to delete.
     * @return true if the deletion was successful, false otherwise.
     */
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

    /**
     * Retrieves a list of SensorSystems by sensor name.
     *
     * @param sensorName The name of the sensor.
     * @return A list of SensorSystems with the specified sensor name.
     */
    @Override
    public List<SensorSystem> findAllSensorSystemsBySensorName(String sensorName) {
        return sensorSystemRepository
                .findDistinctBySensorNameContainingIgnoreCaseOrderBySensorNameAsc(sensorName);
    }

    /**
     * Finds recent statistics by SensorSystem ID.
     *
     * @param sensorSystemId The ID of the SensorSystem.
     * @return A map containing recent statistics for the SensorSystem.
     */
    @Override
    public Map<Integer, Long> findRecentStatsBySensorSystemId(UUID sensorSystemId) {
        List<Integer> timePeriods = Arrays.asList(1, 3, 8, 24, 168);
        Map<Integer, Long> recentReadingCounts = new HashMap<>();
        for (Integer timePeriod : timePeriods) {

            long count =
                    environmentalReadingRepository.countBySensorSystemIdAndTimestampBetween(
                            sensorSystemId,
                            OffsetDateTime.now(ZoneOffset.UTC).minusHours(timePeriod),
                            OffsetDateTime.now(ZoneOffset.UTC));
            recentReadingCounts.put(timePeriod, count);
        }
        return recentReadingCounts;
    }

    /**
     * Retrieves a list of SensorSystems by sensor name and UnconvUserId.
     *
     * @param sensorName The name of the sensor.
     * @param unconvUserId The ID of the UnconvUser.
     * @return A list of SensorSystems with the specified sensor name and UnconvUserId.
     */
    @Override
    public List<SensorSystem> findAllBySensorSystemsBySensorNameAndUnconvUserId(
            String sensorName, UUID unconvUserId) {
        return sensorSystemRepository
                .findDistinctBySensorNameContainsIgnoreCaseAndUnconvUserIdOrderBySensorNameAsc(
                        sensorName, unconvUserId);
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
        long readingCount =
                environmentalReadingRepository.countBySensorSystemId(sensorSystem.getId());
        sensorSystemDTO.setReadingCount(readingCount);
        if (readingCount != 0) {
            EnvironmentalReading environmentalReading =
                    environmentalReadingRepository.findFirstBySensorSystemIdOrderByTimestampDesc(
                            sensorSystem.getId());

            sensorSystemDTO.setLatestReading(
                    modelMapper.map(environmentalReading, BaseEnvironmentalReadingDTO.class));
        }
        return sensorSystemDTO;
    }

    /**
     * Resolves a {@link SensorLocation} reference by retrieving it from the repository if it has an
     * existing ID.
     *
     * <p>This method ensures that a managed {@code SensorLocation} entity is returned only if a
     * valid ID is provided. If the ID is {@code null} or not found, {@code null} is returned.
     *
     * <p>Note: If this method returns {@code null}, persistence may still occur via the owning
     * entity's {@code @ManyToOne(cascade = ALL)} mapping.
     *
     * @param sensorLocation the {@code SensorLocation} to resolve; may be {@code null}
     * @return the resolved {@code SensorLocation} from the database, or {@code null}
     */
    private SensorLocation resolveSensorLocationReference(SensorLocation sensorLocation) {
        if (sensorLocation == null) {
            return null;
        }

        if (sensorLocation.getId() != null) {
            Optional<SensorLocation> optionalSensorLocation =
                    sensorLocationRepository.findById(sensorLocation.getId());
            return optionalSensorLocation.orElse(null);
        } else {
            return null;
        }
    }
}
