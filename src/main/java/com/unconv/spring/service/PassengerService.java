package com.unconv.spring.service;

import com.unconv.spring.domain.Passenger;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;

public interface PassengerService {
    PagedResult<Passenger> findAllPassengers(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<Passenger> findPassengerById(Long id);

    Optional<Passenger> findPassengerByFirstNameIgnoreCase(String firstName);

    Passenger savePassenger(Passenger passenger);

    void deletePassengerById(Long id);
}
