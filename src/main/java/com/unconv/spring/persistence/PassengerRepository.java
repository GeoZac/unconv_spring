package com.unconv.spring.persistence;

import com.unconv.spring.domain.Passenger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Optional<Passenger> findByFirstNameIgnoreCase(String firstName);
}
