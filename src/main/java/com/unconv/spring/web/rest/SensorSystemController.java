package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.SensorSystemService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class responsible for handling HTTP requests related to {@link SensorSystem}. It
 * provides endpoints for managing sensor systems.
 */
@RestController
@RequestMapping("/SensorSystem")
@Slf4j
public class SensorSystemController {

    @Autowired private SensorSystemService sensorSystemService;

    @Autowired private ModelMapper modelMapper;

    /**
     * Retrieves a paginated list of sensor systems.
     *
     * @param pageNo The page number to retrieve (default is 0).
     * @param pageSize The size of each page (default is 10).
     * @param sortBy The field to sort by (default is "sensorName").
     * @param sortDir The direction of sorting (default is "asc" for ascending).
     * @return A {@link PagedResult} containing the paginated list of {@link SensorSystemDTO}s.
     */
    @GetMapping
    public PagedResult<SensorSystemDTO> getAllSensorSystems(
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SS_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SS_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return sensorSystemService.findAllSensorSystems(pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Retrieves a paginated list of sensor systems associated with a specific Unconv user.
     *
     * @param unconvUserId The UUID of the unconventional user.
     * @param pageNo The page number to retrieve (default is 0).
     * @param pageSize The size of each page (default is 10).
     * @param sortBy The field to sort by (default is "sensorName").
     * @param sortDir The direction of sorting (default is "asc" for ascending).
     * @return A {@link PagedResult} containing the paginated list of {@link SensorSystemDTO}s.
     */
    @GetMapping("UnconvUser/{unconvUserId}")
    public PagedResult<SensorSystemDTO> getAllSensorSystemsByUnconvUserId(
            @PathVariable UUID unconvUserId,
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SS_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SS_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return sensorSystemService.findAllSensorSystemsByUnconvUserId(
                unconvUserId, pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorSystemDTO> getSensorSystemById(@PathVariable UUID id) {
        return sensorSystemService
                .findSensorSystemDTOById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/SensorName/{sensorName}")
    public List<SensorSystem> findAllSensorSystemsBySensorName(@PathVariable String sensorName) {
        return sensorSystemService.findAllSensorSystemsBySensorName(sensorName);
    }

    @GetMapping("/ReadingsCount/{sensorSystemId}")
    public Map<Integer, Long> findRecentReadingsCountBySensorSystem(
            @PathVariable UUID sensorSystemId) {
        return sensorSystemService.findRecentStatsBySensorSystemId(sensorSystemId);
    }

    @GetMapping("/SensorName/{sensorName}/UnconvUser/{unconvUserId}")
    public List<SensorSystem> findAllSensorSystemsBySensorNameAndUnconvUserId(
            @PathVariable String sensorName, @PathVariable UUID unconvUserId) {
        return sensorSystemService.findAllBySensorSystemsBySensorNameAndUnconvUserId(
                sensorName, unconvUserId);
    }

    @PostMapping
    public ResponseEntity<MessageResponse<SensorSystemDTO>> createSensorSystem(
            @RequestBody @Validated SensorSystemDTO sensorSystemDTO,
            Authentication authentication) {
        sensorSystemDTO.setId(null);
        return sensorSystemService.validateUnconvUserAndSaveSensorSystem(
                sensorSystemDTO, authentication);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorSystem> updateSensorSystem(
            @PathVariable UUID id, @RequestBody @Valid SensorSystemDTO sensorSystemDTO) {
        return sensorSystemService
                .findSensorSystemById(id)
                .map(
                        sensorSystemObj -> {
                            sensorSystemDTO.setId(id);
                            sensorSystemDTO.setUnconvUser(sensorSystemObj.getUnconvUser());
                            return ResponseEntity.ok(
                                    sensorSystemService.saveSensorSystem(
                                            modelMapper.map(sensorSystemDTO, SensorSystem.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SensorSystem> deleteSensorSystem(@PathVariable UUID id) {
        return sensorSystemService
                .findSensorSystemById(id)
                .map(
                        sensorSystem -> {
                            boolean wasDeleted = sensorSystemService.deleteSensorSystemById(id);
                            if (!wasDeleted) {
                                Optional<SensorSystem> deletedSensorSystem =
                                        sensorSystemService.findSensorSystemById(id);
                                return ResponseEntity.ok(deletedSensorSystem.get());
                            }
                            sensorSystem.setDeleted(true);
                            return ResponseEntity.ok(sensorSystem);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
