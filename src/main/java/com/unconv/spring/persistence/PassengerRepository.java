package com.unconv.spring.persistence;

import com.unconv.spring.domain.Passenger;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Optional<Passenger> findByFirstNameIgnoreCase(String firstName);

    long removeById(Long id);
}
