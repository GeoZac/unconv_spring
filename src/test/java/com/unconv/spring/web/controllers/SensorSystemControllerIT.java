package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.DEFAULT_PAGE_SIZE;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.service.UnconvUserService;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

class SensorSystemControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private UnconvUserService unconvUserService;

    private static final int defaultPageSize = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    private List<SensorSystem> sensorSystemList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorSystem")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        sensorSystemRepository.deleteAllInBatch();

        sensorSystemList = Instancio.ofList(SensorSystem.class).size(6).create();
        totalPages = (int) Math.ceil((double) sensorSystemList.size() / defaultPageSize);
        sensorSystemList = sensorSystemRepository.saveAll(sensorSystemList);
    }

    @Test
    void shouldFetchAllSensorSystemsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorSystem").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.totalElements", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(sensorSystemList.size() < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(sensorSystemList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllSensorSystemsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorSystem").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.totalElements", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(sensorSystemList.size() < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(sensorSystemList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindSensorSystemById() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        UUID sensorSystemId = sensorSystem.getId();

        this.mockMvc
                .perform(get("/SensorSystem/{id}", sensorSystemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystem.getId().toString())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldCreateNewSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "Test user", "testuser@email.com", "test_password");
        UnconvUser savedUnconvUser = unconvUserService.saveUnconvUser(unconvUser);
        SensorSystem sensorSystem =
                new SensorSystem(null, "New SensorSystem", null, savedUnconvUser);
        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(jsonPath("$.unconvUser.username", is(savedUnconvUser.getUsername())));
    }

    @Test
    void shouldReturn400WhenCreateNewSensorSystemWithoutText() throws Exception {
        SensorSystem sensorSystem = new SensorSystem(null, null, null, null);

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("sensorName")))
                .andExpect(jsonPath("$.violations[0].message", is("Sensor name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorSystem() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        sensorSystem.setSensorName("Updated SensorSystem");

        this.mockMvc
                .perform(
                        put("/SensorSystem/{id}", sensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystem.getId().toString())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldDeleteSensorSystem() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);

        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystem.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystem.getId().toString())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/SensorSystem/{id}", sensorSystemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        SensorSystem sensorSystem = sensorSystemList.get(1);

        this.mockMvc
                .perform(
                        put("/SensorSystem/{id}", sensorSystemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystemId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
