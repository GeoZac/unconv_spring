package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.entities.Heater;
import com.unconv.spring.repositories.HeaterRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class HeaterControllerIT extends AbstractIntegrationTest {

    @Autowired private HeaterRepository heaterRepository;

    private List<Heater> heaterList = null;

    @BeforeEach
    void setUp() {
        heaterRepository.deleteAll();

        heaterList = new ArrayList<>();
        heaterList.add(new Heater(1L, "First Heater"));
        heaterList.add(new Heater(2L, "Second Heater"));
        heaterList.add(new Heater(3L, "Third Heater"));
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
                .andExpect(jsonPath("$.text", is(heater.getText())));
    }

    @Test
    void shouldCreateNewHeater() throws Exception {
        Heater heater = new Heater(null, "New Heater");
        this.mockMvc
                .perform(
                        post("/Heater")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(heater.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewHeaterWithoutText() throws Exception {
        Heater heater = new Heater(null, null);

        this.mockMvc
                .perform(
                        post("/Heater")
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
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateHeater() throws Exception {
        Heater heater = heaterList.get(0);
        heater.setText("Updated Heater");

        this.mockMvc
                .perform(
                        put("/Heater/{id}", heater.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(heater.getText())));
    }

    @Test
    void shouldDeleteHeater() throws Exception {
        Heater heater = heaterList.get(0);

        this.mockMvc
                .perform(delete("/Heater/{id}", heater.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(heater.getText())));
    }
}
