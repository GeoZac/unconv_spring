package com.unconv.spring.persistence;

import com.unconv.spring.domain.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    boolean removeById(Long id);
}
