package com.unconv.spring.service.impl;

import com.unconv.spring.domain.Booking;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.BookingRepository;
import com.unconv.spring.service.BookingService;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public PagedResult<Booking> findAllBookings(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Booking> bookingsPage = bookingRepository.findAll(pageable);

        return new PagedResult<>(bookingsPage);
    }

    @Override
    public Optional<Booking> findBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public void deleteBookingById(Long id) {
        bookingRepository.deleteById(id);
    }
}
