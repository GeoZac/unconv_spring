package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.Passenger;
import com.unconv.spring.service.PassengerService;
import com.unconv.spring.web.rest.PassengerController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = PassengerController.class)
@ActiveProfiles(PROFILE_TEST)
class PassengerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PassengerService passengerService;

    @Autowired private ObjectMapper objectMapper;

    private List<Passenger> passengerList;

    @BeforeEach
    void setUp() {
        this.passengerList = new ArrayList<>();
        this.passengerList.add(new Passenger(1L, "text 1"));
        this.passengerList.add(new Passenger(2L, "text 2"));
        this.passengerList.add(new Passenger(3L, "text 3"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllPassengers() throws Exception {
        given(passengerService.findAllPassengers()).willReturn(this.passengerList);

        this.mockMvc
                .perform(get("/Passenger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(passengerList.size())));
    }

    @Test
    void shouldFindPassengerById() throws Exception {
        Long passengerId = 1L;
        Passenger passenger = new Passenger(passengerId, "text 1");
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.of(passenger));

        this.mockMvc
                .perform(get("/Passenger/{id}", passengerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(passenger.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPassenger() throws Exception {
        Long passengerId = 1L;
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/Passenger/{id}", passengerId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPassenger() throws Exception {
        given(passengerService.savePassenger(any(Passenger.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Passenger passenger = new Passenger(1L, "some text");
        this.mockMvc
                .perform(
                        post("/Passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
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
        Long passengerId = 1L;
        Passenger passenger = new Passenger(passengerId, "Updated text");
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.of(passenger));
        given(passengerService.savePassenger(any(Passenger.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/Passenger/{id}", passenger.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(passenger.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPassenger() throws Exception {
        Long passengerId = 1L;
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.empty());
        Passenger passenger = new Passenger(passengerId, "Updated text");

        this.mockMvc
                .perform(
                        put("/Passenger/{id}", passengerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePassenger() throws Exception {
        Long passengerId = 1L;
        Passenger passenger = new Passenger(passengerId, "Some text");
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.of(passenger));
        doNothing().when(passengerService).deletePassengerById(passenger.getId());

        this.mockMvc
                .perform(delete("/Passenger/{id}", passenger.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(passenger.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPassenger() throws Exception {
        Long passengerId = 1L;
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/Passenger/{id}", passengerId))
                .andExpect(status().isNotFound());
    }
}
