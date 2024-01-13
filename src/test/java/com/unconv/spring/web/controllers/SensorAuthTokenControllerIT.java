package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
import com.unconv.spring.persistence.SensorAuthTokenRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    @Autowired private SensorAuthTokenRepository sensorAuthTokenRepository;

    private List<SensorAuthToken> sensorAuthTokenList = null;

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

        sensorAuthTokenList = new ArrayList<>();
        sensorAuthTokenList =
                Instancio.ofList(SensorAuthToken.class)
                        .size(7)
                        .ignore(field(SensorAuthToken::getId))
                        .supply(
                                field(SensorAuthToken::getAuthToken),
                                () -> RandomStringUtils.random(25))
                        .supply(
                                field(SensorAuthToken::getExpiry),
                                () -> OffsetDateTime.now().plusDays(10))
                        .create();
        sensorAuthTokenList = sensorAuthTokenRepository.saveAll(sensorAuthTokenList);
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
        SensorAuthToken sensorAuthToken = sensorAuthTokenList.get(0);
        UUID sensorAuthTokenId = sensorAuthToken.getId();

        this.mockMvc
                .perform(get("/SensorAuthToken/{id}", sensorAuthTokenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorAuthToken.getId().toString())))
                .andExpect(jsonPath("$.authToken", is(sensorAuthToken.getAuthToken())));
    }

    @Test
    void shouldCreateNewSensorAuthToken() throws Exception {
        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        null, RandomStringUtils.random(25), OffsetDateTime.now().plusDays(30));
        this.mockMvc
                .perform(
                        post("/SensorAuthToken")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.authToken", is(sensorAuthToken.getAuthToken())));
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
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("authToken")))
                .andExpect(jsonPath("$.violations[0].message", is("Auth token cannot be empty")))
                .andExpect(jsonPath("$.violations[1].field", is("expiry")))
                .andExpect(jsonPath("$.violations[1].message", is("Expiry cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorAuthToken() throws Exception {
        SensorAuthToken sensorAuthToken = sensorAuthTokenList.get(0);
        sensorAuthToken.setAuthToken("Updated SensorAuthToken");

        this.mockMvc
                .perform(
                        put("/SensorAuthToken/{id}", sensorAuthToken.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorAuthToken.getId().toString())))
                .andExpect(jsonPath("$.authToken", is(sensorAuthToken.getAuthToken())));
    }

    @Test
    void shouldDeleteSensorAuthToken() throws Exception {
        SensorAuthToken sensorAuthToken = sensorAuthTokenList.get(0);

        this.mockMvc
                .perform(delete("/SensorAuthToken/{id}", sensorAuthToken.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorAuthToken.getId().toString())))
                .andExpect(jsonPath("$.authToken", is(sensorAuthToken.getAuthToken())));
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
        SensorAuthToken sensorAuthToken = sensorAuthTokenList.get(1);

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

    @AfterEach
    void tearDown() {
        sensorAuthTokenRepository.deleteAll();
    }
}
