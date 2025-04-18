package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.SensorAuthTokenDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SensorAuthTokenServiceImplTest {

    @Spy private ModelMapper modelMapper;
    @Mock private SensorAuthTokenRepository sensorAuthTokenRepository;

    @InjectMocks private SensorAuthTokenServiceImpl sensorAuthTokenService;

    private SensorAuthToken sensorAuthToken;
    private UUID sensorAuthTokenId;

    @BeforeEach
    void setUp() {
        sensorAuthTokenId = UUID.randomUUID();
        sensorAuthToken = new SensorAuthToken();
        sensorAuthToken.setId(sensorAuthTokenId);
    }

    @Test
    void findAllSensorAuthTokensInAscendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<SensorAuthToken> sensorAuthTokenList = Collections.singletonList(sensorAuthToken);
        Page<SensorAuthToken> sensorAuthTokenPage = new PageImpl<>(sensorAuthTokenList);

        when(sensorAuthTokenRepository.findAll(any(Pageable.class)))
                .thenReturn(sensorAuthTokenPage);

        PagedResult<SensorAuthToken> result =
                sensorAuthTokenService.findAllSensorAuthTokens(pageNo, pageSize, sortBy, sortDir);

        assertEquals(sensorAuthTokenList.size(), result.data().size());
        assertEquals(sensorAuthTokenList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllSensorAuthTokensInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
        List<SensorAuthToken> sensorAuthTokenList = Collections.singletonList(sensorAuthToken);
        Page<SensorAuthToken> sensorAuthTokenPage = new PageImpl<>(sensorAuthTokenList);

        when(sensorAuthTokenRepository.findAll(any(Pageable.class)))
                .thenReturn(sensorAuthTokenPage);

        PagedResult<SensorAuthToken> result =
                sensorAuthTokenService.findAllSensorAuthTokens(pageNo, pageSize, sortBy, sortDir);

        assertEquals(sensorAuthTokenList.size(), result.data().size());
        assertEquals(sensorAuthTokenList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findSensorAuthTokenById() {
        when(sensorAuthTokenRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sensorAuthToken));

        Optional<SensorAuthToken> result =
                sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId);

        assertTrue(result.isPresent());
        assertEquals(sensorAuthToken.getId(), result.get().getId());
    }

    @Test
    void findSensorAuthTokenDTOById() {
        when(sensorAuthTokenRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sensorAuthToken));

        Optional<SensorAuthTokenDTO> result =
                sensorAuthTokenService.findSensorAuthTokenDTOById(sensorAuthTokenId);

        assertTrue(result.isPresent());
        assertEquals(sensorAuthToken.getId(), result.get().getId());
    }

    @Test
    void findSensorAuthTokenDTOByIdWhenEmpty() {
        when(sensorAuthTokenRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Optional<SensorAuthTokenDTO> result =
                sensorAuthTokenService.findSensorAuthTokenDTOById(sensorAuthTokenId);

        assertFalse(result.isPresent());
    }

    @Test
    void saveSensorAuthToken() {
        when(sensorAuthTokenRepository.saveAndFlush(any(SensorAuthToken.class)))
                .thenReturn(sensorAuthToken);

        sensorAuthToken.setAuthToken("sensorAuthToken");

        SensorAuthToken result = sensorAuthTokenService.saveSensorAuthToken(sensorAuthToken);

        assertEquals(sensorAuthToken.getId(), result.getId());
    }

    @Test
    void saveSensorAuthTokenDTO() {
        when(sensorAuthTokenRepository.saveAndFlush(any(SensorAuthToken.class)))
                .thenReturn(sensorAuthToken);

        sensorAuthToken.setAuthToken("sensorAuthToken");

        SensorAuthTokenDTO result = sensorAuthTokenService.saveSensorAuthTokenDTO(sensorAuthToken);

        assertEquals(sensorAuthToken.getId(), result.getId());
    }

    @Test
    void deleteSensorAuthTokenById() {
        sensorAuthTokenService.deleteSensorAuthTokenById(sensorAuthTokenId);

        verify(sensorAuthTokenRepository, times(1)).deleteById(sensorAuthTokenId);
    }

    @Test
    void deleteAnyExistingSensorSystem() {
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setId(UUID.randomUUID());
        sensorAuthToken.setSensorSystem(sensorSystem);

        when(sensorAuthTokenRepository.findBySensorSystemId(any(UUID.class))).thenReturn(null);

        sensorAuthTokenService.deleteAnyExistingSensorSystem(sensorSystem.getId());

        verify(sensorAuthTokenRepository, times(1)).findBySensorSystemId(sensorSystem.getId());
    }

    @Test
    void deleteAnyExistingSensorSystemWhenSensorAuthTokenExists() {
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setId(UUID.randomUUID());
        sensorAuthToken.setSensorSystem(sensorSystem);

        when(sensorAuthTokenRepository.findBySensorSystemId(any(UUID.class)))
                .thenReturn(sensorAuthToken);

        sensorAuthTokenService.deleteAnyExistingSensorSystem(sensorSystem.getId());

        verify(sensorAuthTokenRepository, times(1)).findBySensorSystemId(sensorSystem.getId());
    }

    @Test
    void generateSensorAuthToken() {
        SensorSystem sensorSystem = new SensorSystem();

        when(sensorAuthTokenRepository.saveAndFlush(any(SensorAuthToken.class)))
                .thenReturn(sensorAuthToken);

        SensorAuthTokenDTO result =
                sensorAuthTokenService.generateSensorAuthToken(sensorSystem, sensorAuthTokenId);

        assertEquals(sensorAuthToken.getId(), result.getId());
    }

    @Test
    void generateSensorAuthTokenWithNullSensorAuthTokenId() {
        SensorSystem sensorSystem = new SensorSystem();

        when(sensorAuthTokenRepository.saveAndFlush(any(SensorAuthToken.class)))
                .thenReturn(sensorAuthToken);

        SensorAuthTokenDTO result =
                sensorAuthTokenService.generateSensorAuthToken(sensorSystem, null);

        assertEquals(sensorAuthToken.getId(), result.getId());
    }

    @Test
    void getSensorAuthTokenInfo() {
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setId(UUID.randomUUID());

        when(sensorAuthTokenRepository.findBySensorSystemId(sensorSystem.getId()))
                .thenReturn(sensorAuthToken);

        SensorAuthTokenDTO result = sensorAuthTokenService.getSensorAuthTokenInfo(sensorSystem);

        assertEquals(sensorAuthToken.getId(), result.getId());
    }

    @Test
    void getSensorAuthTokenInfoWhenEmpty() {
        SensorSystem sensorSystem = new SensorSystem();
        sensorSystem.setId(UUID.randomUUID());

        when(sensorAuthTokenRepository.findBySensorSystemId(sensorSystem.getId())).thenReturn(null);

        SensorAuthTokenDTO result = sensorAuthTokenService.getSensorAuthTokenInfo(sensorSystem);

        assertNull(result);
    }

    @Test
    void generateUniqueSaltedSuffix() {
        when(sensorAuthTokenRepository.findByTokenHashAllIgnoreCase(any(String.class)))
                .thenReturn(null);

        String result = sensorAuthTokenService.generateUniqueSaltedSuffix();

        assertNotNull(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sensorAuthTokenRepository).findByTokenHashAllIgnoreCase(captor.capture());

        String calledTokenHash = captor.getValue();

        assertEquals(calledTokenHash, result);
    }

    @Test
    void generateUniqueSaltedSuffixWhenFirstGeneratedTokenIsNotUniqueShouldGenerateAnother() {
        SensorAuthToken existingToken = new SensorAuthToken();

        when(sensorAuthTokenRepository.findByTokenHashAllIgnoreCase(any(String.class)))
                .thenReturn(existingToken)
                .thenReturn(null);

        String result = sensorAuthTokenService.generateUniqueSaltedSuffix();

        assertNotNull(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sensorAuthTokenRepository, times(2)).findByTokenHashAllIgnoreCase(captor.capture());

        List<String> allGenerated = captor.getAllValues();

        assertNotEquals(allGenerated.get(0), allGenerated.get(1));

        assertEquals(allGenerated.get(1), result);
    }
}
