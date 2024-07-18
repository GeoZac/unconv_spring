package com.unconv.spring.web.controllers;

import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static com.unconv.spring.matchers.SensorAuthTokenMatcher.validSensorAuthToken;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.SensorAuthTokenDTO;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.SensorAuthTokenService;
import com.unconv.spring.service.UnconvUserService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

class SensorAuthTokenControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private UnconvUserService unconvUserService;

    @Autowired private SensorAuthTokenService sensorAuthTokenService;

    @Autowired private UnconvUserRepository unconvUserRepository;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private SensorAuthTokenRepository sensorAuthTokenRepository;

    private SensorSystem savedSensorSystem;

    private List<SensorAuthTokenDTO> sensorAuthTokenList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorAuthToken")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        sensorAuthTokenRepository.deleteAllInBatch();

        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        List<SensorSystem> sensorSystemList =
                Instancio.ofList(SensorSystem.class)
                        .size(7)
                        .ignore(field(SensorSystem::getSensorLocation))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .set(field(SensorSystem::getUnconvUser), savedUnconvUser)
                        .create();

        sensorSystemList = sensorSystemRepository.saveAll(sensorSystemList);

        sensorAuthTokenList = new ArrayList<>();
        for (SensorSystem sensorSystem : sensorSystemList) {
            SensorAuthTokenDTO sensorAuthTokenDTO =
                    sensorAuthTokenService.generateSensorAuthToken(sensorSystem, null);
            sensorAuthTokenList.add(sensorAuthTokenDTO);
        }
    }

    @Test
    void shouldFetchAllSensorAuthTokensInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorAuthToken").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorAuthTokenList.size())))
                .andExpect(jsonPath("$.totalElements", is(sensorAuthTokenList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllSensorAuthTokensInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorAuthToken").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorAuthTokenList.size())))
                .andExpect(jsonPath("$.totalElements", is(sensorAuthTokenList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindSensorAuthTokenById() throws Exception {
        SensorAuthTokenDTO sensorAuthToken = sensorAuthTokenList.get(0);
        UUID sensorAuthTokenId = sensorAuthToken.getId();

        this.mockMvc
                .perform(get("/SensorAuthToken/{id}", sensorAuthTokenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorAuthToken.getId().toString())))
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(true)))
                .andReturn();
    }

    @Test
    void shouldCreateNewSensorAuthToken() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        SensorSystem sensorSystem = new SensorSystem(null, "Test sensor", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        null,
                        RandomStringUtils.random(25),
                        OffsetDateTime.now().plusDays(30),
                        savedSensorSystem);
        this.mockMvc
                .perform(
                        post("/SensorAuthToken")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(false)))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewSensorAuthTokenWithNullValues() throws Exception {
        SensorAuthToken sensorAuthToken = new SensorAuthToken();

        this.mockMvc
                .perform(
                        post("/SensorAuthToken")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("authToken")))
                .andExpect(jsonPath("$.violations[0].message", is("Auth token cannot be empty")))
                .andExpect(jsonPath("$.violations[1].field", is("expiry")))
                .andExpect(jsonPath("$.violations[1].message", is("Expiry cannot be empty")))
                .andExpect(jsonPath("$.violations[2].field", is("sensorSystem")))
                .andExpect(jsonPath("$.violations[2].message", is("Sensor system cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorAuthToken() throws Exception {
        SensorAuthTokenDTO sensorAuthToken = sensorAuthTokenList.get(0);
        sensorAuthToken.setAuthToken("Updated SensorAuthToken");
        sensorAuthToken.setExpiry(OffsetDateTime.now().plusDays(100));

        this.mockMvc
                .perform(
                        put("/SensorAuthToken/{id}", sensorAuthToken.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorAuthToken.getId().toString())))
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(false)))
                .andReturn();
    }

    @Test
    void shouldDeleteSensorAuthToken() throws Exception {
        SensorAuthTokenDTO sensorAuthToken = sensorAuthTokenList.get(0);

        this.mockMvc
                .perform(delete("/SensorAuthToken/{id}", sensorAuthToken.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorAuthToken.getId().toString())))
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(true)))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/SensorAuthToken/{id}", sensorAuthTokenId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        SensorAuthTokenDTO sensorAuthToken = sensorAuthTokenList.get(1);

        this.mockMvc
                .perform(
                        put("/SensorAuthToken/{id}", sensorAuthTokenId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/SensorAuthToken/{id}", sensorAuthTokenId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGenerateAndReturnNewSensorTokenForAValidSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        SensorSystem sensorSystem = new SensorSystem(null, "Test sensor", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/GenerateToken/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Generated New Sensor Auth Token")))
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.authToken", validSensorAuthToken(false)))
                .andExpect(
                        jsonPath("$.entity.sensorSystem.id", is(sensorSystem.getId().toString())))
                .andReturn();
    }

    @Test
    void shouldGenerateAndReturnNewSensorTokenForAValidSensorSystemWithGeneratedAuthToken()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        SensorSystem sensorSystem = new SensorSystem(null, "Test sensor", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        SensorAuthTokenDTO sensorAuthToken =
                sensorAuthTokenService.generateSensorAuthToken(savedSensorSystem, null);

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/GenerateToken/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Generated New Sensor Auth Token")))
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.id", not(sensorAuthToken.getId().toString())))
                .andExpect(jsonPath("$.entity.authToken", validSensorAuthToken(false)))
                .andExpect(
                        jsonPath("$.entity.sensorSystem.id", is(sensorSystem.getId().toString())))
                .andReturn();

        SensorAuthToken persistedSensorAuthToken =
                sensorAuthTokenRepository.findBySensorSystemId(savedSensorSystem.getId());
        assert persistedSensorAuthToken.getId() != sensorAuthToken.getId();
    }

    @Test
    void shouldReturn404WhenRequestingTokenForANonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/GenerateToken/SensorSystem/{sensorSystemId}",
                                        sensorSystemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldReturn400WhenRequestingTokenForAnInActiveSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        SensorSystem sensorSystem = new SensorSystem(null, "Test sensor", null, savedUnconvUser);
        sensorSystem.setSensorStatus(SensorStatus.INACTIVE);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        SensorAuthTokenDTO sensorAuthToken =
                sensorAuthTokenService.generateSensorAuthToken(savedSensorSystem, null);

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/GenerateToken/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Sensor Inactive or Deleted")))
                .andExpect(jsonPath("$.entity", CoreMatchers.nullValue()))
                .andReturn();
    }

    @Test
    void shouldReturnSensorTokenInfoForAValidSensorSystemWithSensorAuthToken() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        SensorSystem sensorSystem = new SensorSystem(null, "Test sensor", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        UUID sensorAuthTokenUUID =
                sensorAuthTokenService.generateSensorAuthToken(savedSensorSystem, null).getId();

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id", is(sensorAuthTokenUUID.toString())))
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(true)))
                .andExpect(jsonPath("$.expiry", notNullValue(OffsetDateTime.class)))
                .andExpect(jsonPath("$.sensorSystem.id", is(sensorSystem.getId().toString())))
                .andReturn();
    }

    @Test
    void shouldReturnSensorTokenInfoForAValidSensorSystemWithoutSensorAuthToken() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        SensorSystem sensorSystem = new SensorSystem(null, "Test sensor", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.id", nullValue()))
                .andExpect(jsonPath("$.authToken", nullValue()))
                .andExpect(jsonPath("$.expiry", nullValue()))
                .andExpect(jsonPath("$.sensorSystem.id", is(sensorSystem.getId().toString())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenRequestingTokenInfoForANonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        this.mockMvc
                .perform(
                        get("/SensorAuthToken/SensorSystem/{sensorSystemId}", sensorSystemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @AfterEach
    void tearDown() {
        sensorAuthTokenRepository.deleteAll();
        sensorSystemRepository.deleteAll();
        unconvUserRepository.deleteAll();
    }
}
