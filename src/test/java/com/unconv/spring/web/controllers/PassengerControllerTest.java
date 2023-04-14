package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
import com.unconv.spring.consts.Gender;
import com.unconv.spring.domain.Passenger;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.PassengerService;
import com.unconv.spring.web.rest.PassengerController;

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = PassengerController.class)
@ActiveProfiles(PROFILE_TEST)
class PassengerControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private PassengerService passengerService;

    @Autowired private ObjectMapper objectMapper;

    private List<Passenger> passengerList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/Passenger")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.passengerList = new ArrayList<>();
        this.passengerList.add(
                new Passenger(
                        1L,
                        "Robert",
                        null,
                        "Langdon",
                        0,
                        LocalDate.of(1972, 8, 13),
                        Gender.MALE,
                        null));
        this.passengerList.add(
                new Passenger(
                        2L,
                        "Katherine",
                        null,
                        "Brewster",
                        LocalDate.of(1988, 5, 9),
                        Gender.FEMALE));
        this.passengerList.add(
                new Passenger(
                        3L, "Loki", null, "Laufeyson", LocalDate.of(1022, 4, 23), Gender.OTHER));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllPassengers() throws Exception {
        Page<Passenger> page = new PageImpl<>(passengerList);
        PagedResult<Passenger> passengerPagedResult = new PagedResult<>(page);
        given(passengerService.findAllPassengers(0, 10, "id", "asc"))
                .willReturn(passengerPagedResult);

        this.mockMvc
                .perform(get("/Passenger"))
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
        Long passengerId = 1L;
        Passenger passenger =
                new Passenger(
                        passengerId,
                        "Pierce",
                        null,
                        "Bronsnan",
                        LocalDate.of(1953, 12, 4),
                        Gender.MALE);
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.of(passenger));

        this.mockMvc
                .perform(get("/Passenger/{id}", passengerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(passenger.getFirstName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPassenger() throws Exception {
        Long passengerId = 1L;
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/Passenger/{id}", passengerId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenSearchingNonExistingPassengerByFirstName() throws Exception {
        String notExistingName = "ZZZZZZZZZ";
        given(passengerService.findPassengerByFirstNameIgnoreCase(notExistingName))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/Passenger/search/firstName/{firstName}", notExistingName))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPassenger() throws Exception {
        given(passengerService.savePassenger(any(Passenger.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Passenger passenger =
                new Passenger(
                        1L, "Pablo", "Ruiz", "Picasso", LocalDate.of(1952, 7, 2), Gender.MALE);
        this.mockMvc
                .perform(
                        post("/Passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
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
        Long passengerId = 1L;
        Passenger passenger =
                new Passenger(
                        passengerId,
                        "Mary",
                        null,
                        "Magdalene",
                        LocalDate.of(1991, 2, 28),
                        Gender.FEMALE);

        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.of(passenger));
        given(passengerService.savePassenger(any(Passenger.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/Passenger/{id}", passenger.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(passenger.getFirstName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPassenger() throws Exception {
        Long passengerId = 1L;
        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.empty());
        Passenger passenger =
                new Passenger(
                        3L, "Tom", "Marvelo", "Riddle", LocalDate.of(1872, 12, 1), Gender.MALE);

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
        Passenger passenger =
                new Passenger(
                        passengerId,
                        "Ian",
                        null,
                        " Malcolm",
                        LocalDate.of(1977, 11, 20),
                        Gender.MALE);

        given(passengerService.findPassengerById(passengerId)).willReturn(Optional.of(passenger));

        this.mockMvc
                .perform(delete("/Passenger/{id}", passenger.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(passenger.getFirstName())));
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
