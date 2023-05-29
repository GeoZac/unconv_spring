package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
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
import com.unconv.spring.consts.SensorLocationType;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.persistence.SensorLocationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class SensorLocationControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private SensorLocationRepository sensorLocationRepository;

    private List<SensorLocation> sensorLocationList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorLocation")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        sensorLocationRepository.deleteAllInBatch();

        sensorLocationList = new ArrayList<>();
        sensorLocationList.add(
                new SensorLocation(
                        null,
                        "Great Pyramid of Giza",
                        29.9792,
                        31.1342,
                        SensorLocationType.INDOOR));
        sensorLocationList.add(
                new SensorLocation(
                        null, "Stonehenge", 51.1789, -1.8262, SensorLocationType.OUTDOOR));
        sensorLocationList.add(
                new SensorLocation(
                        null, "Machu Picchu", -13.1631, -72.5450, SensorLocationType.INDOOR));
        sensorLocationList = sensorLocationRepository.saveAll(sensorLocationList);
    }

    @Test
    void shouldFetchAllSensorLocationsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorLocation").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorLocationList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllSensorLocationsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorLocation").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorLocationList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindSensorLocationById() throws Exception {
        SensorLocation sensorLocation = sensorLocationList.get(0);
        UUID sensorLocationId = sensorLocation.getId();

        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorLocation.getId().toString())))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldCreateNewSensorLocation() throws Exception {
        SensorLocation sensorLocation =
                new SensorLocation(null, "Petra", 30.3285, 35.4414, SensorLocationType.OUTDOOR);
        this.mockMvc
                .perform(
                        post("/SensorLocation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn400WhenCreateNewSensorLocationWithoutText() throws Exception {
        SensorLocation sensorLocation = new SensorLocation(null, null, null, null, null);

        this.mockMvc
                .perform(
                        post("/SensorLocation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("sensorLocationText")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Sensor location text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorLocation() throws Exception {
        SensorLocation sensorLocation = sensorLocationList.get(0);
        sensorLocation.setSensorLocationText("Updated SensorLocation");

        this.mockMvc
                .perform(
                        put("/SensorLocation/{id}", sensorLocation.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorLocation.getId().toString())))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldDeleteSensorLocation() throws Exception {
        SensorLocation sensorLocation = sensorLocationList.get(0);

        this.mockMvc
                .perform(delete("/SensorLocation/{id}", sensorLocation.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorLocation.getId().toString())))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        SensorLocation sensorLocation = sensorLocationList.get(1);

        this.mockMvc
                .perform(
                        put("/SensorLocation/{id}", sensorLocationId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/SensorLocation/{id}", sensorLocationId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
