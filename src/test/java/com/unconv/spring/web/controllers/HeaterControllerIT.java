package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
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
import com.unconv.spring.domain.Heater;
import com.unconv.spring.persistence.HeaterRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class HeaterControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private HeaterRepository heaterRepository;

    private List<Heater> heaterList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/Heater")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        heaterRepository.deleteAll();

        heaterList = new ArrayList<>();
        heaterList.add(new Heater(1L, 34F, 2F));
        heaterList.add(new Heater(2L, 40F, 1F));
        heaterList.add(new Heater(3L, 35F, 5F));
        heaterList = heaterRepository.saveAll(heaterList);
    }

    @Test
    void shouldFetchAllHeaters() throws Exception {
        this.mockMvc
                .perform(get("/Heater"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(heaterList.size())));
    }

    @Test
    void shouldFindHeaterById() throws Exception {
        Heater heater = heaterList.get(0);
        Long heaterId = heater.getId();

        this.mockMvc
                .perform(get("/Heater/{id}", heaterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(heater.getTemperature()), Float.class));
    }

    @Test
    void shouldCreateNewHeater() throws Exception {
        Heater heater = new Heater(null, 20F, 0.5F);
        this.mockMvc
                .perform(
                        post("/Heater")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.temperature", is(heater.getTemperature()), Float.class));
    }

    @Test
    void shouldReturn400WhenCreateNewHeaterWithNullValues() throws Exception {
        Heater heater = new Heater(null, null, null);

        this.mockMvc
                .perform(
                        post("/Heater")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("tempTolerance")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Heater temperature tolerance cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateHeater() throws Exception {
        Heater heater = heaterList.get(0);
        heater.setTemperature(27F);

        this.mockMvc
                .perform(
                        put("/Heater/{id}", heater.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(heater.getTemperature()), Float.class));
    }

    @Test
    void shouldReturn400WhenUpdatingInvalidHeater() throws Exception {
        Heater updatedHeater = heaterList.get(0);
        updatedHeater.setTempTolerance(null);
        updatedHeater.setTempTolerance(null);

        this.mockMvc
                .perform(
                        put("/Heater/{id}", updatedHeater.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedHeater)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("tempTolerance")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Heater temperature tolerance cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldDeleteHeater() throws Exception {
        Heater heater = heaterList.get(0);

        this.mockMvc
                .perform(delete("/Heater/{id}", heater.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(heater.getTemperature()), Float.class));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingHeater() throws Exception {
        this.mockMvc.perform(get("/Heater/{id}", 0L)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingHeater() throws Exception {
        Heater heater = new Heater();
        heater.setId(0L);
        heater.setTemperature(27F);
        heater.setTempTolerance(2F);

        this.mockMvc
                .perform(
                        put("/Heater/{id}", heater.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingHeater() throws Exception {
        this.mockMvc
                .perform(delete("/Heater/{id}", 0L).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
