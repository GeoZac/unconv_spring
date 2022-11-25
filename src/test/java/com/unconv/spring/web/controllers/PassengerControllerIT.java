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
import com.unconv.spring.domain.Passenger;
import com.unconv.spring.persistence.PassengerRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PassengerControllerIT extends AbstractIntegrationTest {

    @Autowired private PassengerRepository passengerRepository;

    private List<Passenger> passengerList = null;

    @BeforeEach
    void setUp() {
        passengerRepository.deleteAll();

        passengerList = new ArrayList<>();
        passengerList.add(new Passenger(1L, "First Passenger"));
        passengerList.add(new Passenger(2L, "Second Passenger"));
        passengerList.add(new Passenger(3L, "Third Passenger"));
        passengerList = passengerRepository.saveAll(passengerList);
    }

    @Test
    void shouldFetchAllPassengers() throws Exception {
        this.mockMvc
                .perform(get("/Passenger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(passengerList.size())));
    }

    @Test
    void shouldFindPassengerById() throws Exception {
        Passenger passenger = passengerList.get(0);
        Long passengerId = passenger.getId();

        this.mockMvc
                .perform(get("/Passenger/{id}", passengerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(passenger.getText())));
    }

    @Test
    void shouldCreateNewPassenger() throws Exception {
        Passenger passenger = new Passenger(null, "New Passenger");
        this.mockMvc
                .perform(
                        post("/Passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(passenger.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewPassengerWithoutText() throws Exception {
        Passenger passenger = new Passenger(null, null);

        this.mockMvc
                .perform(
                        post("/Passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
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
    void shouldUpdatePassenger() throws Exception {
        Passenger passenger = passengerList.get(0);
        passenger.setText("Updated Passenger");

        this.mockMvc
                .perform(
                        put("/Passenger/{id}", passenger.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(passenger.getText())));
    }

    @Test
    void shouldDeletePassenger() throws Exception {
        Passenger passenger = passengerList.get(0);

        this.mockMvc
                .perform(delete("/Passenger/{id}", passenger.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(passenger.getText())));
    }
}
