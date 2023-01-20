package com.unconv.spring.web.rest;

import com.unconv.spring.domain.Booking;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.BookingService;
import com.unconv.spring.utils.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Booking")
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public PagedResult<Booking> getAllBookings(
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return bookingService.findAllBookings(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return bookingService
                .findBookingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@RequestBody @Validated Booking booking) {
        return bookingService.saveBooking(booking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(
            @PathVariable Long id, @RequestBody Booking booking) {
        return bookingService
                .findBookingById(id)
                .map(
                        bookingObj -> {
                            booking.setId(id);
                            return ResponseEntity.ok(bookingService.saveBooking(booking));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Booking> deleteBooking(@PathVariable Long id) {
        return bookingService
                .findBookingById(id)
                .map(
                        booking -> {
                            bookingService.deleteBookingById(id);
                            return ResponseEntity.ok(booking);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
