package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.consts.SensorLocationType;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.SensorLocationService;
import com.unconv.spring.web.rest.SensorLocationController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebMvcTest(controllers = SensorLocationController.class)
@ActiveProfiles(PROFILE_TEST)
class SensorLocationControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private SensorLocationService sensorLocationService;

    @Autowired private ObjectMapper objectMapper;

    private List<SensorLocation> sensorLocationList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorLocation")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.sensorLocationList = new ArrayList<>();
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

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllSensorLocations() throws Exception {
        Page<SensorLocation> page = new PageImpl<>(sensorLocationList);
        PagedResult<SensorLocation> sensorLocationPagedResult = new PagedResult<>(page);
        given(sensorLocationService.findAllSensorLocations(0, 10, "id", "asc"))
                .willReturn(sensorLocationPagedResult);

        this.mockMvc
                .perform(get("/SensorLocation"))
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
        UUID sensorLocationId = UUID.randomUUID();
        SensorLocation sensorLocation =
                new SensorLocation(null, "Petra", 30.3285, 35.4414, SensorLocationType.OUTDOOR);
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.of(sensorLocation));

        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewSensorLocation() throws Exception {
        given(sensorLocationService.saveSensorLocation(any(SensorLocation.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        SensorLocation sensorLocation =
                new SensorLocation(
                        UUID.randomUUID(),
                        "Moai Statues",
                        -27.1212,
                        -109.3667,
                        SensorLocationType.OUTDOOR);
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
        UUID sensorLocationId = UUID.randomUUID();
        SensorLocation sensorLocation =
                new SensorLocation(
                        sensorLocationId, " Alhambra", 37.1760, -3.5875, SensorLocationType.INDOOR);
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.of(sensorLocation));
        given(sensorLocationService.saveSensorLocation(any(SensorLocation.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/SensorLocation/{id}", sensorLocation.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.empty());
        SensorLocation sensorLocation =
                new SensorLocation(
                        sensorLocationId,
                        "Angkor Wat",
                        13.4125,
                        103.8667,
                        SensorLocationType.INDOOR);

        this.mockMvc
                .perform(
                        put("/SensorLocation/{id}", sensorLocationId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        SensorLocation sensorLocation =
                new SensorLocation(
                        sensorLocationId,
                        " Hagia Sophia",
                        41.0082,
                        28.9784,
                        SensorLocationType.INDOOR);
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.of(sensorLocation));
        doNothing().when(sensorLocationService).deleteSensorLocationById(sensorLocation.getId());

        this.mockMvc
                .perform(delete("/SensorLocation/{id}", sensorLocation.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/SensorLocation/{id}", sensorLocationId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
