package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.persistence.HumidityThresholdRepository;
import com.unconv.spring.persistence.TemperatureThresholdRepository;
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

class TemperatureThresholdControllerIT extends AbstractIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private TemperatureThresholdRepository temperatureThresholdRepository;

    @Autowired private HumidityThresholdRepository humidityThresholdRepository;

    private List<TemperatureThreshold> temperatureThresholdList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/TemperatureThreshold")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        temperatureThresholdRepository.deleteAllInBatch();
        final int setUpListSize = 7;
        temperatureThresholdList =
                Instancio.ofList(TemperatureThreshold.class)
                        .size(setUpListSize)
                        .ignore(field(TemperatureThreshold::getId))
                        .generate(
                                field(TemperatureThreshold::getMinValue),
                                gen -> gen.doubles().range(-9999.000, -1.0))
                        .generate(
                                field(TemperatureThreshold::getMaxValue),
                                gen -> gen.doubles().range(1.0, 9999.000))
                        .create();
        temperatureThresholdList = temperatureThresholdRepository.saveAll(temperatureThresholdList);

        assert temperatureThresholdList.size() == setUpListSize;

        List<HumidityThreshold> humidityThresholdList =
                Instancio.ofList(HumidityThreshold.class)
                        .size(5)
                        .ignore(field(HumidityThreshold::getId))
                        .generate(
                                field(HumidityThreshold::getMinValue),
                                gen -> gen.doubles().range(0.0, 49.0))
                        .generate(
                                field(HumidityThreshold::getMaxValue),
                                gen -> gen.doubles().range(51.0, 100.0))
                        .create();

        humidityThresholdRepository.saveAll(humidityThresholdList);
    }

    @Test
    void shouldFetchAllTemperatureThresholdsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/TemperatureThreshold").param("sortDir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(temperatureThresholdList.size())))
                .andExpect(jsonPath("$.totalElements", is(temperatureThresholdList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllTemperatureThresholdsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/TemperatureThreshold").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(temperatureThresholdList.size())))
                .andExpect(jsonPath("$.totalElements", is(temperatureThresholdList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindTemperatureThresholdById() throws Exception {
        TemperatureThreshold temperatureThreshold = temperatureThresholdList.get(0);
        UUID temperatureThresholdId = temperatureThreshold.getId();

        this.mockMvc
                .perform(get("/TemperatureThreshold/{id}", temperatureThresholdId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(temperatureThresholdId.toString())))
                .andExpect(
                        jsonPath(
                                "$.minValue", is(temperatureThreshold.getMinValue()), Double.class))
                .andExpect(
                        jsonPath(
                                "$.maxValue",
                                is(temperatureThreshold.getMaxValue()),
                                Double.class));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/TemperatureThreshold/{id}", temperatureThresholdId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewTemperatureThreshold() throws Exception {
        TemperatureThreshold temperatureThreshold = new TemperatureThreshold(null, 100, 0);
        this.mockMvc
                .perform(
                        post("/TemperatureThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.minValue", is(temperatureThreshold.getMinValue())))
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn400WhenCreateNewTemperatureThresholdWithMaxValueLessThanMinValue()
            throws Exception {
        TemperatureThreshold temperatureThreshold = new TemperatureThreshold(null, 20, 80);
        this.mockMvc
                .perform(
                        post("/TemperatureThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("temperatureThresholdDTO")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Min. value must be less than Max. value")))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewTemperatureThresholdWithImproperCoordinatesInPositiveRange()
            throws Exception {
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(null, 10000.0, 10000.0);
        this.mockMvc
                .perform(
                        post("/TemperatureThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[1].field", is("minValue")))
                .andExpect(
                        jsonPath(
                                "$.violations[1].message",
                                is("Min value must be less than or equal to 9999")))
                .andExpect(jsonPath("$.violations[0].field", is("maxValue")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Max value must be less than or equal to 9999")))
                .andExpect(jsonPath("$.violations[2].field", is("temperatureThresholdDTO")))
                .andExpect(
                        jsonPath(
                                "$.violations[2].message",
                                is("Min. value must be less than Max. value")))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewTemperatureThresholdWithImproperCoordinatesInNegativeRange()
            throws Exception {
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(null, -10000.0, -10000.0);
        this.mockMvc
                .perform(
                        post("/TemperatureThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[1].field", is("minValue")))
                .andExpect(
                        jsonPath(
                                "$.violations[1].message",
                                is("Min value must be greater than or equal to -9999")))
                .andExpect(jsonPath("$.violations[0].field", is("maxValue")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Max value must be greater than or equal to -9999")))
                .andExpect(jsonPath("$.violations[2].field", is("temperatureThresholdDTO")))
                .andExpect(
                        jsonPath(
                                "$.violations[2].message",
                                is("Min. value must be less than Max. value")))
                .andReturn();
    }

    @Test
    void shouldUpdateTemperatureThreshold() throws Exception {
        TemperatureThreshold temperatureThreshold = temperatureThresholdList.get(0);
        temperatureThreshold.setMaxValue(50);

        this.mockMvc
                .perform(
                        put("/TemperatureThreshold/{id}", temperatureThreshold.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        TemperatureThreshold temperatureThreshold = temperatureThresholdList.get(1);

        this.mockMvc
                .perform(
                        put("/TemperatureThreshold/{id}", temperatureThresholdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTemperatureThreshold() throws Exception {
        TemperatureThreshold temperatureThreshold = temperatureThresholdList.get(0);

        this.mockMvc
                .perform(
                        delete("/TemperatureThreshold/{id}", temperatureThreshold.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/TemperatureThreshold/{id}", temperatureThresholdId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void tearDown() {
        temperatureThresholdRepository.deleteAll();
    }
}
