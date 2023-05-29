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
import com.unconv.spring.domain.Booking;
import com.unconv.spring.domain.Passenger;
import com.unconv.spring.persistence.BookingRepository;
import com.unconv.spring.persistence.PassengerRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class BookingControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private BookingRepository bookingRepository;

    @Autowired private PassengerRepository passengerRepository;

    private List<Booking> bookingList = null;
    private List<Passenger> passengerList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/Booking")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        bookingRepository.deleteAllInBatch();

        passengerList = new ArrayList<>();
        passengerList.add(
                new com.unconv.spring.domain.Passenger(
                        1L,
                        "Robert",
                        null,
                        "Langdon",
                        java.time.LocalDate.of(1972, 8, 13),
                        com.unconv.spring.consts.Gender.MALE,
                        null));
        passengerList.add(
                new com.unconv.spring.domain.Passenger(
                        2L,
                        "Katherine",
                        null,
                        "Brewster",
                        java.time.LocalDate.of(1988, 5, 9),
                        com.unconv.spring.consts.Gender.FEMALE,
                        null));
        passengerList.add(
                new com.unconv.spring.domain.Passenger(
                        3L,
                        "Tom",
                        "Marvelo",
                        "Riddle",
                        java.time.LocalDate.of(1872, 12, 1),
                        com.unconv.spring.consts.Gender.OTHER,
                        null));
        passengerList = passengerRepository.saveAll(passengerList);

        bookingList = new ArrayList<>();
        bookingList.add(new Booking(1L, "First Booking", passengerList));
        bookingList.add(new Booking(2L, "Second Booking", passengerList));
        bookingList.add(new Booking(3L, "Third Booking", passengerList));
        bookingList = bookingRepository.saveAll(bookingList);
    }

    @Test
    void shouldFetchAllBookingsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/Booking").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(bookingList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllBookingsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/Booking").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(bookingList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindBookingById() throws Exception {
        Booking booking = bookingList.get(0);
        Long bookingId = booking.getId();

        this.mockMvc
                .perform(get("/Booking/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking", is(booking.getBooking())));
    }

    @Test
    void shouldCreateNewBooking() throws Exception {
        Booking booking = new Booking(null, "New Booking", passengerList);
        this.mockMvc
                .perform(
                        post("/Booking")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.booking", is(booking.getBooking())))
                .andExpect(jsonPath("$.passengers.size()", is(booking.getPassengers().size())));
    }

    @Test
    void shouldReturn400WhenCreateNewBookingWithoutText() throws Exception {
        Booking booking = new Booking(4L, null, passengerList);

        this.mockMvc
                .perform(
                        post("/Booking")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("booking")))
                .andExpect(jsonPath("$.violations[0].message", is("Booking cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateBooking() throws Exception {
        Booking booking = bookingList.get(0);
        booking.setBooking("Updated Booking");

        this.mockMvc
                .perform(
                        put("/Booking/{id}", booking.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking", is(booking.getBooking())));
    }

    @Test
    void shouldDeleteBooking() throws Exception {
        Booking booking = bookingList.get(0);

        this.mockMvc
                .perform(delete("/Booking/{id}", booking.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking", is(booking.getBooking())));
    }
}
