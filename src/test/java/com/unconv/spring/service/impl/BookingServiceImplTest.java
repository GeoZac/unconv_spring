package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.Booking;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.BookingRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;

    @InjectMocks private BookingServiceImpl bookingService;

    private Booking booking;
    private Long bookingId;

    @BeforeEach
    void setUp() {
        bookingId = 1L;
        booking = new Booking();
        booking.setId(bookingId);
    }

    @Test
    void findAllBookings() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<Booking> bookingList = Collections.singletonList(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList);

        when(bookingRepository.findAll(any(Pageable.class))).thenReturn(bookingPage);

        PagedResult<Booking> result =
                bookingService.findAllBookings(pageNo, pageSize, sortBy, sortDir);

        assertEquals(bookingList.size(), result.data().size());
        assertEquals(bookingList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findBookingById() {
        when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(booking));

        Optional<Booking> result = bookingService.findBookingById(bookingId);

        assertTrue(result.isPresent());
        assertEquals(booking.getId(), result.get().getId());
    }

    @Test
    void saveBooking() {
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.saveBooking(booking);

        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void deleteBookingById() {
        bookingService.deleteBookingById(bookingId);

        verify(bookingRepository, times(1)).deleteById(bookingId);
    }
}
