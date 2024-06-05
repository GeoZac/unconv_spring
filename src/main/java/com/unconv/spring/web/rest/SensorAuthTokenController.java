package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.dto.SensorAuthTokenDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.SensorAuthTokenService;
import com.unconv.spring.service.SensorSystemService;
import java.util.UUID;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class responsible for handling HTTP requests related to {@link SensorAuthToken}. It
 * provides endpoints for managing sensor systems.
 */
@RestController
@RequestMapping("/SensorAuthToken")
@Slf4j
public class SensorAuthTokenController {

    private final SensorAuthTokenService sensorAuthTokenService;

    private final SensorSystemService sensorSystemService;

    private final ModelMapper modelMapper;

    /**
     * Constructs a new SensorAuthTokenController with the specified services.
     *
     * @param sensorAuthTokenService the service for sensor authentication tokens
     * @param sensorSystemService the service for sensor systems
     * @param modelMapper the model mapper
     */
    @Autowired
    public SensorAuthTokenController(
            SensorAuthTokenService sensorAuthTokenService,
            SensorSystemService sensorSystemService,
            ModelMapper modelMapper) {
        this.sensorAuthTokenService = sensorAuthTokenService;
        this.sensorSystemService = sensorSystemService;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves a paginated list of all sensor authentication tokens.
     *
     * @param pageNo the page number
     * @param pageSize the size of each page
     * @param sortBy the field to sort by
     * @param sortDir the direction of sorting
     * @return a {@link PagedResult} containing the sensor authentication tokens
     */
    @GetMapping
    public PagedResult<SensorAuthToken> getAllSensorAuthTokens(
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
                            defaultValue = AppConstants.DEFAULT_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return sensorAuthTokenService.findAllSensorAuthTokens(pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Retrieves the sensor authentication token with the specified ID.
     *
     * @param id the ID of the sensor authentication token to retrieve
     * @return a {@link ResponseEntity} containing the sensor authentication token, or not found if
     *     not exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<SensorAuthToken> getSensorAuthTokenById(@PathVariable UUID id) {
        return sensorAuthTokenService
                .findSensorAuthTokenById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves information about the sensor authentication token associated with the specified
     * sensor system ID.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a {@link ResponseEntity} containing the sensor authentication token information, or
     *     not found if the sensor system does not exist
     */
    @GetMapping("/SensorSystem/{sensorSystemId}")
    public ResponseEntity<SensorAuthTokenDTO> getSensorAuthTokenInfo(
            @PathVariable @Validated UUID sensorSystemId) {
        return sensorSystemService
                .findSensorSystemById(sensorSystemId)
                .map(
                        sensorSystemObj -> {
                            SensorAuthTokenDTO sensorAuthToken =
                                    sensorAuthTokenService.getSensorAuthTokenInfo(sensorSystemObj);
                            if (sensorAuthToken == null) {
                                SensorAuthTokenDTO sensorAuthTokenDTO = new SensorAuthTokenDTO();
                                sensorAuthTokenDTO.setSensorSystem(sensorSystemObj);
                                return new ResponseEntity<>(
                                        sensorAuthTokenDTO, HttpStatus.NO_CONTENT);
                            }
                            return new ResponseEntity<>(sensorAuthToken, HttpStatus.OK);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new sensor authentication token.
     *
     * @param sensorAuthTokenDTO the sensor authentication token DTO to create
     * @return the created sensor authentication token DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorAuthTokenDTO createSensorAuthToken(
            @RequestBody @Validated SensorAuthTokenDTO sensorAuthTokenDTO) {
        sensorAuthTokenDTO.setId(null);
        return sensorAuthTokenService.generateSensorAuthToken(
                sensorAuthTokenDTO.getSensorSystem(), null);
    }

    /**
     * Generates a new sensor authentication token for the specified sensor system ID.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return a {@link ResponseEntity} containing a message response with the generated sensor
     *     authentication token, or a not found response if the sensor system does not exist
     */
    @GetMapping("/GenerateToken/SensorSystem/{sensorSystemId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MessageResponse<SensorAuthTokenDTO>> generateSensorAuthToken(
            @PathVariable @Validated UUID sensorSystemId) {
        return sensorSystemService
                .findSensorSystemById(sensorSystemId)
                .map(
                        sensorSystemObj -> {
                            SensorAuthTokenDTO sensorAuthToken =
                                    sensorAuthTokenService.generateSensorAuthToken(
                                            sensorSystemObj, null);
                            return new ResponseEntity<>(
                                    new MessageResponse<>(
                                            sensorAuthToken, "Generated New Sensor Auth Token"),
                                    HttpStatus.CREATED);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Updates the sensor authentication token with the specified ID.
     *
     * @param id the ID of the sensor authentication token to update
     * @param sensorAuthTokenDTO the updated sensor authentication token DTO
     * @return a {@link ResponseEntity} containing the updated sensor authentication token DTO if
     *     found, or not found if the token does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<SensorAuthTokenDTO> updateSensorAuthToken(
            @PathVariable UUID id, @RequestBody @Valid SensorAuthTokenDTO sensorAuthTokenDTO) {
        return sensorAuthTokenService
                .findSensorAuthTokenById(id)
                .map(
                        sensorAuthTokenObj -> {
                            SensorAuthTokenDTO updatedSensorAuthTokenDTO =
                                    sensorAuthTokenService.generateSensorAuthToken(
                                            sensorAuthTokenDTO.getSensorSystem(), id);
                            return ResponseEntity.ok(updatedSensorAuthTokenDTO);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes the sensor authentication token with the specified ID.
     *
     * @param id the ID of the sensor authentication token to delete
     * @return a {@link ResponseEntity} indicating success if the token was deleted, or not found if
     *     the token does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SensorAuthToken> deleteSensorAuthToken(@PathVariable UUID id) {
        return sensorAuthTokenService
                .findSensorAuthTokenById(id)
                .map(
                        sensorAuthToken -> {
                            sensorAuthTokenService.deleteSensorAuthTokenById(id);
                            return ResponseEntity.ok(sensorAuthToken);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
