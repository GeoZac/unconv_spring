package com.unconv.spring.service.impl;

import static com.unconv.spring.consts.SensorAuthConstants.TOKEN_LENGTH;
import static com.unconv.spring.consts.SensorAuthConstants.TOKEN_PREFIX;
import static com.unconv.spring.utils.SaltedSuffixGenerator.generateSaltedSuffix;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorAuthTokenDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import com.unconv.spring.service.SensorAuthTokenService;
import com.unconv.spring.utils.AccessTokenGenerator;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SensorAuthTokenServiceImpl implements SensorAuthTokenService {

    private final SensorAuthTokenRepository sensorAuthTokenRepository;

    private final ModelMapper modelMapper;

    /**
     * Constructs an instance of {@link SensorAuthTokenServiceImpl} with the specified dependencies.
     *
     * @param sensorAuthTokenRepository the repository for managing sensor authentication tokens
     * @param modelMapper the mapper for converting between DTOs and entities
     */
    @Autowired
    public SensorAuthTokenServiceImpl(
            SensorAuthTokenRepository sensorAuthTokenRepository, ModelMapper modelMapper) {
        this.sensorAuthTokenRepository = sensorAuthTokenRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves a paginated list of SensorAuthTokens.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of SensorAuthTokens.
     */
    @Override
    public PagedResult<SensorAuthToken> findAllSensorAuthTokens(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<SensorAuthToken> sensorAuthTokensPage = sensorAuthTokenRepository.findAll(pageable);

        return new PagedResult<>(sensorAuthTokensPage);
    }

    /**
     * Retrieves a SensorAuthToken by its ID.
     *
     * @param id The ID of the SensorAuthToken.
     * @return An Optional containing the SensorAuthToken, or empty if not found.
     */
    @Override
    public Optional<SensorAuthToken> findSensorAuthTokenById(UUID id) {
        return sensorAuthTokenRepository.findById(id);
    }

    /**
     * Retrieves a SensorAuthTokenDTO by its ID.
     *
     * @param id The ID of the SensorAuthToken.
     * @return An Optional containing the SensorAuthTokenDTO, or empty if not found.
     */
    @Override
    public Optional<SensorAuthTokenDTO> findSensorAuthTokenDTOById(UUID id) {
        Optional<SensorAuthToken> optionalSensorAuthToken = sensorAuthTokenRepository.findById(id);
        if (optionalSensorAuthToken.isPresent()) {
            SensorAuthToken sensorAuthToken = optionalSensorAuthToken.get();
            String maskedAuthToken =
                    TOKEN_PREFIX + "*".repeat(TOKEN_LENGTH) + sensorAuthToken.getTokenHash();
            SensorAuthTokenDTO sensorAuthTokenDTO =
                    modelMapper.map(sensorAuthToken, SensorAuthTokenDTO.class);
            sensorAuthTokenDTO.setAuthToken(maskedAuthToken);
            return Optional.of(sensorAuthTokenDTO);
        }
        return Optional.empty();
    }

    /**
     * Saves a SensorAuthToken.
     *
     * @param sensorAuthToken The SensorAuthToken to save.
     * @return The saved SensorAuthToken.
     */
    @Override
    public SensorAuthToken saveSensorAuthToken(SensorAuthToken sensorAuthToken) {
        sensorAuthToken.setAuthToken(
                bCryptPasswordEncoder().encode(sensorAuthToken.getAuthToken()));
        return sensorAuthTokenRepository.saveAndFlush(sensorAuthToken);
    }

    /**
     * Saves a SensorAuthToken.
     *
     * @param sensorAuthToken The SensorAuthToken to save.
     * @return The saved SensorAuthTokenDTO.
     */
    @Override
    public SensorAuthTokenDTO saveSensorAuthTokenDTO(SensorAuthToken sensorAuthToken) {
        String authToken = sensorAuthToken.getAuthToken();
        SensorAuthToken savedSensorAuthToken = saveSensorAuthToken(sensorAuthToken);
        SensorAuthTokenDTO savedSensorAuthTokenDTO =
                modelMapper.map(savedSensorAuthToken, SensorAuthTokenDTO.class);
        savedSensorAuthTokenDTO.setAuthToken(authToken);
        return savedSensorAuthTokenDTO;
    }

    /**
     * Deletes a SensorAuthToken by its ID.
     *
     * @param id The ID of the SensorAuthToken to delete.
     */
    @Override
    public void deleteSensorAuthTokenById(UUID id) {
        sensorAuthTokenRepository.deleteById(id);
    }

    /**
     * Deletes any SensorAuthToken by the SensoorSystem id.
     *
     * @param sensorSystemId The ID of the SensorSystem to delete SensorAuthTokens of.
     */
    @Override
    public void deleteAnyExistingSensorSystem(UUID sensorSystemId) {
        SensorAuthToken sensorAuthToken =
                sensorAuthTokenRepository.findBySensorSystemId(sensorSystemId);
        if (sensorAuthToken == null) {
            return;
        }
        deleteSensorAuthTokenById(sensorAuthToken.getId());
    }

    /**
     * Generates an authentication token for the given SensorSystem.
     *
     * @param sensorSystem The SensorSystem for which the token is generated.
     * @param sensorAuthTokenId The SensorAuthToken, in case the request is for updating
     * @return The generated SensorAuthTokenDTO.
     */
    @Override
    public SensorAuthTokenDTO generateSensorAuthToken(
            SensorSystem sensorSystem, UUID sensorAuthTokenId) {
        SensorAuthToken sensorAuthToken = new SensorAuthToken();
        if (sensorAuthTokenId != null) {
            sensorAuthToken.setId(sensorAuthTokenId);
        } else {
            deleteAnyExistingSensorSystem(sensorSystem.getId());
        }
        String generatedString = AccessTokenGenerator.generateAccessToken();
        String generatedSaltedSuffix = generateUniqueSaltedSuffix();
        sensorAuthToken.setSensorSystem(sensorSystem);
        sensorAuthToken.setAuthToken(generatedString + generatedSaltedSuffix);
        sensorAuthToken.setTokenHash(generatedSaltedSuffix);
        sensorAuthToken.setExpiry(OffsetDateTime.now().plusDays(60));
        SensorAuthToken savedSensorAuthToken = saveSensorAuthToken(sensorAuthToken);
        SensorAuthTokenDTO savedSensorAuthTokenDTO =
                modelMapper.map(savedSensorAuthToken, SensorAuthTokenDTO.class);
        savedSensorAuthTokenDTO.setAuthToken(generatedString + generatedSaltedSuffix);
        return savedSensorAuthTokenDTO;
    }

    /**
     * Retrieves information about the authentication token for the given SensorSystem.
     *
     * @param sensorSystem The SensorSystem for which to retrieve token information.
     * @return The SensorAuthTokenDTO containing token information.
     */
    @Override
    public SensorAuthTokenDTO getSensorAuthTokenInfo(SensorSystem sensorSystem) {
        SensorAuthToken sensorAuthToken =
                sensorAuthTokenRepository.findBySensorSystemId(sensorSystem.getId());
        if (sensorAuthToken == null) {
            return null;
        }
        String maskedAuthToken =
                TOKEN_PREFIX + "*".repeat(TOKEN_LENGTH) + sensorAuthToken.getTokenHash();
        SensorAuthTokenDTO sensorAuthTokenDTO =
                modelMapper.map(sensorAuthToken, SensorAuthTokenDTO.class);
        sensorAuthTokenDTO.setAuthToken(maskedAuthToken);
        return sensorAuthTokenDTO;
    }

    /**
     * Generates a unique salted suffix for authentication tokens.
     *
     * @return A unique salted suffix.
     */
    @Override
    public String generateUniqueSaltedSuffix() {
        boolean uniqueSensorAuthToken = false;
        String sensorAuthTokenString;
        do {
            sensorAuthTokenString = generateSaltedSuffix();
            SensorAuthToken sensorAuthToken =
                    sensorAuthTokenRepository.findByTokenHashAllIgnoreCase(sensorAuthTokenString);
            if (sensorAuthToken == null) uniqueSensorAuthToken = true;
        } while (!uniqueSensorAuthToken);
        return sensorAuthTokenString;
    }

    /**
     * Bean definition for creating a {@link BCryptPasswordEncoder} instance.
     *
     * @return a new instance of {@link BCryptPasswordEncoder} for encoding passwords using BCrypt
     *     hashing
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
