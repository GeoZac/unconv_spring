package com.unconv.spring.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.unconv.spring.domain.Booking;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.BookingRepository;
import com.unconv.spring.service.BookingService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;

    @InjectMocks private BookingService bookingService;

    @Test
    void findAllBookings() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Booking> bookingPage = new PageImpl<>(List.of(getBooking()));
        given(bookingRepository.findAll(pageable)).willReturn(bookingPage);

        // when
        PagedResult<Booking> pagedResult = bookingService.findAllBookings(0, 10, "id", "asc");

        // then
        assertThat(pagedResult).isNotNull();
        assertThat(pagedResult.data()).isNotEmpty().hasSize(1);
        assertThat(pagedResult.hasNext()).isFalse();
        assertThat(pagedResult.pageNumber()).isEqualTo(1);
        assertThat(pagedResult.totalPages()).isEqualTo(1);
        assertThat(pagedResult.isFirst()).isTrue();
        assertThat(pagedResult.isLast()).isTrue();
        assertThat(pagedResult.hasPrevious()).isFalse();
        assertThat(pagedResult.totalElements()).isEqualTo(1);
    }

    @Test
    void findBookingById() {
        // given
        given(bookingRepository.findById(1L)).willReturn(Optional.of(getBooking()));
        // when
        Optional<Booking> optionalBooking = bookingService.findBookingById(1L);
        // then
        assertThat(optionalBooking).isPresent();
        Booking booking = optionalBooking.get();
        assertThat(booking.getId()).isEqualTo(1L);
        assertThat(booking.getText()).isEqualTo("junitTest");
    }

    @Test
    void saveBooking() {
        // given
        given(bookingRepository.save(getBooking())).willReturn(getBooking());
        // when
        Booking persistedBooking = bookingService.saveBooking(getBooking());
        // then
        assertThat(persistedBooking).isNotNull();
        assertThat(persistedBooking.getId()).isEqualTo(1L);
        assertThat(persistedBooking.getText()).isEqualTo("junitTest");
    }

    @Test
    void deleteBookingById() {
        // given
        willDoNothing().given(bookingRepository).deleteById(1L);
        // when
        bookingService.deleteBookingById(1L);
        // then
        verify(bookingRepository, times(1)).deleteById(1L);
    }

    private Booking getBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setText("junitTest");
        return booking;
    }
}
