package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.DEFAULT_PAGE_SIZE;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
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
import com.unconv.spring.consts.SensorLocationType;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.service.EnvironmentalReadingService;

import net.minidev.json.JSONArray;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class EnvironmentalReadingControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    @Autowired private EnvironmentalReadingService environmentalReadingService;

    private List<EnvironmentalReading> environmentalReadingList = null;

    private static final int defaultPageSize = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/EnvironmentalReading")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        environmentalReadingRepository.deleteAllInBatch();

        environmentalReadingList =
                Instancio.ofList(EnvironmentalReading.class)
                        .size(15)
                        .ignore(field(EnvironmentalReading::getId))
                        .create();

        totalPages = (int) Math.ceil((double) environmentalReadingList.size() / defaultPageSize);

        environmentalReadingList = environmentalReadingRepository.saveAll(environmentalReadingList);
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/EnvironmentalReading").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(defaultPageSize)))
                .andExpect(jsonPath("$.totalElements", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(
                        jsonPath("$.isLast", is(environmentalReadingList.size() < defaultPageSize)))
                .andExpect(
                        jsonPath(
                                "$.hasNext", is(environmentalReadingList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/EnvironmentalReading").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(defaultPageSize)))
                .andExpect(jsonPath("$.totalElements", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(
                        jsonPath("$.isLast", is(environmentalReadingList.size() < defaultPageSize)))
                .andExpect(
                        jsonPath(
                                "$.hasNext", is(environmentalReadingList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindEnvironmentalReadingById() throws Exception {
        EnvironmentalReading environmentalReading = environmentalReadingList.get(0);
        UUID environmentalReadingId = environmentalReading.getId();

        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(environmentalReading.getId().toString())))
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldCreateNewEnvironmentalReading() throws Exception {
        SensorLocation sensorLocation =
                new SensorLocation(
                        null, "Taj Mahal", 27.1751448, 78.0421422, SensorLocationType.INDOOR);
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor A", sensorLocation);
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        null,
                        3L,
                        56L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 3, 17, 7, 9), ZoneOffset.UTC),
                        sensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldReturn400WhenCreateNewEnvironmentalReadingWithoutText() throws Exception {
        SensorLocation sensorLocation =
                new SensorLocation(
                        null, "Colosseum", 41.8902102, 12.4922309, SensorLocationType.OUTDOOR);
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor A", sensorLocation);
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(UUID.randomUUID(), 0L, 0L, null, sensorSystem);

        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("timestamp")))
                .andExpect(jsonPath("$.violations[0].message", is("Timestamp cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateEnvironmentalReading() throws Exception {
        EnvironmentalReading environmentalReading = environmentalReadingList.get(0);
        environmentalReading.setTemperature(45L);

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReading.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(environmentalReading.getId().toString())))
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldDeleteEnvironmentalReading() throws Exception {
        EnvironmentalReading environmentalReading = environmentalReadingList.get(0);

        this.mockMvc
                .perform(
                        delete("/EnvironmentalReading/{id}", environmentalReading.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(environmentalReading.getId().toString())))
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        EnvironmentalReading environmentalReading = environmentalReadingList.get(1);

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReadingId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/EnvironmentalReading/{id}", environmentalReadingId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForQuarterHourly() throws Exception {
        Map<OffsetDateTime, Double> averageTemperatures = setupTestDataForQuarterHourly();
        averageTemperatures.size();
        this.mockMvc
                .perform(get("/EnvironmentalReading/QuarterHourly").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForQuarterHourly() {
        List<EnvironmentalReading> environmentalReadings = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            EnvironmentalReading environmentalReading =
                    Instancio.of(EnvironmentalReading.class)
                            .supply(
                                    field(EnvironmentalReading::getTimestamp),
                                    random ->
                                            ZonedDateTime.of(
                                                            LocalDateTime.now()
                                                                    .minusHours(3)
                                                                    .plusMinutes(
                                                                            random.intRange(
                                                                                    0, 180)),
                                                            ZoneId.systemDefault())
                                                    .toOffsetDateTime())
                            .create();
            environmentalReadings.add(environmentalReading);
        }
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingService.getAverageTempsForQuarterHourly();
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForHourly() throws Exception {
        Map<OffsetDateTime, Double> averageTemperatures = setupTestDataForHourly();
        averageTemperatures.size();
        this.mockMvc
                .perform(get("/EnvironmentalReading/Hourly").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForHourly() {
        List<EnvironmentalReading> environmentalReadings = new ArrayList<>();
        for (int i = 0; i < 75; i++) {
            EnvironmentalReading environmentalReading =
                    Instancio.of(EnvironmentalReading.class)
                            .supply(
                                    field(EnvironmentalReading::getTimestamp),
                                    random ->
                                            ZonedDateTime.of(
                                                            LocalDateTime.now()
                                                                    .minusHours(24)
                                                                    .plusMinutes(
                                                                            random.intRange(
                                                                                    0, 1440)),
                                                            ZoneId.systemDefault())
                                                    .toOffsetDateTime())
                            .create();
            environmentalReadings.add(environmentalReading);
        }
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingService.getAverageTempsForHourly();
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForDaily() throws Exception {
        Map<OffsetDateTime, Double> averageTemperatures = setupTestDataForDaily();
        averageTemperatures.size();
        this.mockMvc
                .perform(get("/EnvironmentalReading/Daily").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForDaily() {
        List<EnvironmentalReading> environmentalReadings = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            EnvironmentalReading environmentalReading =
                    Instancio.of(EnvironmentalReading.class)
                            .supply(
                                    field(EnvironmentalReading::getTimestamp),
                                    random ->
                                            ZonedDateTime.of(
                                                            LocalDateTime.now()
                                                                    .minusDays(7)
                                                                    .plusMinutes(
                                                                            random.intRange(
                                                                                    0, 10080)),
                                                            ZoneId.systemDefault())
                                                    .toOffsetDateTime())
                            .create();
            environmentalReadings.add(environmentalReading);
        }
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingService.getAverageTempsForDaily();
    }
}
