package com.unconv.spring.service;

import com.unconv.spring.domain.Booking;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;

public interface BookingService {
    PagedResult<Booking> findAllBookings(int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<Booking> findBookingById(Long id);

    Booking saveBooking(Booking booking);

    void deleteBookingById(Long id);
}
