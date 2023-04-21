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
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class EnvironmentalReadingControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    private List<EnvironmentalReading> environmentalReadingList = null;

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

        environmentalReadingList = new ArrayList<>();
        this.environmentalReadingList.add(
                new EnvironmentalReading(null, 30L, 45L, LocalDateTime.of(2023, 4, 2, 12, 3)));
        this.environmentalReadingList.add(
                new EnvironmentalReading(null, 30L, 5L, LocalDateTime.of(2023, 3, 27, 7, 9)));
        this.environmentalReadingList.add(
                new EnvironmentalReading(null, 45L, 85L, LocalDateTime.of(2023, 3, 4, 18, 45)));

        environmentalReadingList = environmentalReadingRepository.saveAll(environmentalReadingList);
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/EnvironmentalReading").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/EnvironmentalReading").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
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
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(null, 3L, 56L, LocalDateTime.of(2023, 3, 17, 7, 9));
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
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(UUID.randomUUID(), 0L, 0L, null);

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
}
