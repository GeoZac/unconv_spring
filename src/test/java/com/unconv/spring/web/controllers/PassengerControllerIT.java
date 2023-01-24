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
import com.unconv.spring.consts.Gender;
import com.unconv.spring.domain.Passenger;
import com.unconv.spring.persistence.PassengerRepository;
import java.time.LocalDate;
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
        passengerList.add(
                new Passenger(
                        1L, "Robert", null, "Langdon", LocalDate.of(1972, 8, 13), Gender.MALE));
        passengerList.add(
                new Passenger(
                        2L,
                        "Katherine",
                        null,
                        "Brewster",
                        LocalDate.of(1988, 5, 9),
                        Gender.FEMALE));
        passengerList.add(
                new Passenger(
                        3L, "Tom", "Marvelo", "Riddle", LocalDate.of(1872, 12, 1), Gender.OTHER));
        passengerList = passengerRepository.saveAll(passengerList);
    }

    @Test
    void shouldFetchAllPassengersInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/Passenger").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(passengerList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllPassengersInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/Passenger").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(passengerList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindPassengerById() throws Exception {
        Passenger passenger = passengerList.get(0);
        Long passengerId = passenger.getId();

        this.mockMvc
                .perform(get("/Passenger/{id}", passengerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(passenger.getFirstName())));
    }

    @Test
    void shouldFindPassengerByByFirstNameIgnoringCase() throws Exception {
        Passenger passenger = passengerList.get(0);
        String passengerName = passenger.getFirstName().toLowerCase();

        this.mockMvc
                .perform(get("/Passenger/search/firstName/{firstName}", passengerName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(passenger.getFirstName())));
    }

    @Test
    void shouldCreateNewPassenger() throws Exception {
        Passenger passenger =
                new Passenger(
                        1L, "Pablo", "Ruiz", "Picasso", LocalDate.of(1952, 7, 2), Gender.MALE);
        this.mockMvc
                .perform(
                        post("/Passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(passenger.getFirstName())));
    }

    @Test
    void shouldReturn400WhenCreateNewPassengerWithoutText() throws Exception {
        Passenger passenger = new Passenger(null, null, null, null, 0, null, null, null);

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
                .andExpect(jsonPath("$.violations", hasSize(4)))
                .andExpect(jsonPath("$.violations[0].field", is("dateOfBirth")))
                .andExpect(jsonPath("$.violations[0].message", is("Date of Birth cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdatePassenger() throws Exception {
        Passenger passenger = passengerList.get(0);
        passenger.setFirstName("Updated Passenger");

        this.mockMvc
                .perform(
                        put("/Passenger/{id}", passenger.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(passenger.getFirstName())));
    }

    @Test
    void shouldReturn400WhenUpdatingPassengerWithoutFirstLastName() throws Exception {
        Passenger updatedPassenger = passengerList.get(1);
        updatedPassenger.setFirstName(null);
        updatedPassenger.setLastName(null);

        this.mockMvc
                .perform(
                        put("/Passenger/{id}", updatedPassenger.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedPassenger)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("firstName")))
                .andExpect(jsonPath("$.violations[0].message", is("First name cannot be empty")))
                .andExpect(jsonPath("$.violations[1].field", is("lastName")))
                .andExpect(jsonPath("$.violations[1].message", is("Last name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldDeletePassenger() throws Exception {
        Passenger passenger = passengerList.get(0);

        this.mockMvc
                .perform(delete("/Passenger/{id}", passenger.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(passenger.getFirstName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPassenger() throws Exception {
        Long passengerId = 0L;
        this.mockMvc.perform(get("/Passenger/{id}", passengerId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenSearchingNonExistingPassenger() throws Exception {
        String notExistingName = "ZZZZZZZZZ";
        this.mockMvc
                .perform(get("/Passenger/search/firstName/{firstName}", notExistingName))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPassenger() throws Exception {
        Long passengerId = 0L;
        Passenger passenger =
                new Passenger(
                        passengerId,
                        "Edgar",
                        "Allan",
                        "Poe",
                        LocalDate.of(1809, 1, 19),
                        Gender.MALE);

        this.mockMvc
                .perform(
                        put("/Passenger/{id}", passengerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPassenger() throws Exception {
        Long passengerId = 0L;
        this.mockMvc
                .perform(delete("/Passenger/{id}", passengerId))
                .andExpect(status().isNotFound());
    }
}
