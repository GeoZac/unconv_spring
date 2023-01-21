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
import com.unconv.spring.domain.Booking;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.BookingService;
import com.unconv.spring.web.rest.BookingController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

@WebMvcTest(controllers = BookingController.class)
@ActiveProfiles(PROFILE_TEST)
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private BookingService bookingService;

    @Autowired private ObjectMapper objectMapper;

    private List<Booking> bookingList;

    @BeforeEach
    void setUp() {
        this.bookingList = new ArrayList<>();
        this.bookingList.add(new Booking(1L, "text 1"));
        this.bookingList.add(new Booking(2L, "text 2"));
        this.bookingList.add(new Booking(3L, "text 3"));
    }

    @Test
    void shouldFetchAllBookings() throws Exception {
        Page<Booking> page = new PageImpl<>(bookingList);
        PagedResult<Booking> bookingPagedResult = new PagedResult<>(page);
        given(bookingService.findAllBookings(0, 10, "id", "asc")).willReturn(bookingPagedResult);

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
        Long bookingId = 1L;
        Booking booking = new Booking(bookingId, "text 1");
        given(bookingService.findBookingById(bookingId)).willReturn(Optional.of(booking));

        this.mockMvc
                .perform(get("/Booking/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(booking.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingBooking() throws Exception {
        Long bookingId = 1L;
        given(bookingService.findBookingById(bookingId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/Booking/{id}", bookingId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewBooking() throws Exception {
        given(bookingService.saveBooking(any(Booking.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Booking booking = new Booking(1L, "some text");
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
        Long bookingId = 1L;
        Booking booking = new Booking(bookingId, "Updated text");
        given(bookingService.findBookingById(bookingId)).willReturn(Optional.of(booking));
        given(bookingService.saveBooking(any(Booking.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/Booking/{id}", booking.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(booking.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingBooking() throws Exception {
        Long bookingId = 1L;
        given(bookingService.findBookingById(bookingId)).willReturn(Optional.empty());
        Booking booking = new Booking(bookingId, "Updated text");

        this.mockMvc
                .perform(
                        put("/Booking/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteBooking() throws Exception {
        Long bookingId = 1L;
        Booking booking = new Booking(bookingId, "Some text");
        given(bookingService.findBookingById(bookingId)).willReturn(Optional.of(booking));
        doNothing().when(bookingService).deleteBookingById(booking.getId());

        this.mockMvc
                .perform(delete("/Booking/{id}", booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(booking.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingBooking() throws Exception {
        Long bookingId = 1L;
        given(bookingService.findBookingById(bookingId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/Booking/{id}", bookingId)).andExpect(status().isNotFound());
    }
}
