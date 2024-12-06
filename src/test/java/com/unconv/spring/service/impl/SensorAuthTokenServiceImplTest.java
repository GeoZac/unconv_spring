package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SensorAuthTokenServiceImplTest {
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
    void findAllSensorAuthTokens() {
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
    void findSensorAuthTokenById() {
        when(sensorAuthTokenRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(sensorAuthToken));

        Optional<SensorAuthToken> result =
                sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId);

        assertTrue(result.isPresent());
        assertEquals(sensorAuthToken.getId(), result.get().getId());
    }

    @Test
    void findSensorAuthTokenDTOById() {}

    @Test
    void saveSensorAuthToken() {}

    @Test
    void saveSensorAuthTokenDTO() {}

    @Test
    void deleteSensorAuthTokenById() {}

    @Test
    void deleteAnyExistingSensorSystem() {}

    @Test
    void generateSensorAuthToken() {}

    @Test
    void getSensorAuthTokenInfo() {}

    @Test
    void generateUniqueSaltedSuffix() {}

    @Test
    void bCryptPasswordEncoder() {}
}
