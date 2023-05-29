package com.unconv.spring.persistence;

import com.unconv.spring.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {}
