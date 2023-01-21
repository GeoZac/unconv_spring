package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.domain.Booking;
import com.unconv.spring.persistence.BookingRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class BookingControllerIT extends AbstractIntegrationTest {

    @Autowired private BookingRepository bookingRepository;

    private List<Booking> bookingList = null;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAllInBatch();

        bookingList = new ArrayList<>();
        bookingList.add(new Booking(null, "First Booking"));
        bookingList.add(new Booking(null, "Second Booking"));
        bookingList.add(new Booking(null, "Third Booking"));
        bookingList = bookingRepository.saveAll(bookingList);
    }

    @Test
    void shouldFetchAllBookings() throws Exception {
        this.mockMvc
                .perform(get("/Booking"))
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
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(booking.getText())));
    }

    @Test
    void shouldCreateNewBooking() throws Exception {
        Booking booking = new Booking(null, "New Booking");
        this.mockMvc
                .perform(
                        post("/Booking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(booking.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewBookingWithoutText() throws Exception {
        Booking booking = new Booking(null, null);

        this.mockMvc
                .perform(
                        post("/Booking")
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
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateBooking() throws Exception {
        Booking booking = bookingList.get(0);
        booking.setText("Updated Booking");

        this.mockMvc
                .perform(
                        put("/Booking/{id}", booking.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(booking.getText())));
    }

    @Test
    void shouldDeleteBooking() throws Exception {
        Booking booking = bookingList.get(0);

        this.mockMvc
                .perform(delete("/Booking/{id}", booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(booking.getText())));
    }
}
