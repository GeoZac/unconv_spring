package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

class HumidityThresholdControllerIT extends AbstractIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private HumidityThresholdRepository humidityThresholdRepository;

    @Autowired private TemperatureThresholdRepository temperatureThresholdRepository;

    private List<HumidityThreshold> humidityThresholdList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/HumidityThreshold")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        humidityThresholdRepository.deleteAllInBatch();
        final int setUpListSize = 7;
        humidityThresholdList =
                Instancio.ofList(HumidityThreshold.class)
                        .size(setUpListSize)
                        .ignore(field(HumidityThreshold::getId))
                        .create();
        humidityThresholdList = humidityThresholdRepository.saveAll(humidityThresholdList);

        assert humidityThresholdList.size() == setUpListSize;

        List<TemperatureThreshold> temperatureThresholdList =
                Instancio.ofList(TemperatureThreshold.class)
                        .size(5)
                        .ignore(field(TemperatureThreshold::getId))
                        .create();

        temperatureThresholdRepository.saveAll(temperatureThresholdList);
    }

    @Test
    void shouldFetchAllHumidityThresholdsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/HumidityThreshold").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(humidityThresholdList.size())))
                .andExpect(jsonPath("$.totalElements", is(humidityThresholdList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllHumidityThresholdsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/HumidityThreshold").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(humidityThresholdList.size())))
                .andExpect(jsonPath("$.totalElements", is(humidityThresholdList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindHumidityThresholdById() throws Exception {
        HumidityThreshold humidityThreshold = humidityThresholdList.get(0);
        UUID humidityThresholdId = humidityThreshold.getId();

        this.mockMvc
                .perform(get("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(humidityThresholdId.toString())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())));
    }

    @Test
    void shouldCreateNewHumidityThreshold() throws Exception {
        HumidityThreshold humidityThreshold = new HumidityThreshold(null, 0, 100);
        this.mockMvc
                .perform(
                        post("/HumidityThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())));
    }

    @Test
    void shouldUpdateHumidityThreshold() throws Exception {
        HumidityThreshold humidityThreshold = humidityThresholdList.get(0);
        humidityThreshold.setMaxValue(50);

        this.mockMvc
                .perform(
                        put("/HumidityThreshold/{id}", humidityThreshold.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        HumidityThreshold humidityThreshold = humidityThresholdList.get(1);

        this.mockMvc
                .perform(
                        put("/HumidityThreshold/{id}", humidityThresholdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteHumidityThreshold() throws Exception {
        HumidityThreshold humidityThreshold = humidityThresholdList.get(0);

        this.mockMvc
                .perform(delete("/HumidityThreshold/{id}", humidityThreshold.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void tearDown() {
        humidityThresholdRepository.deleteAll();
    }
}
