package com.unconv.spring.service.impl;

import com.unconv.spring.domain.Passenger;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.PassengerRepository;
import com.unconv.spring.service.PassengerService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PassengerServiceImpl implements PassengerService {

    @Autowired private PassengerRepository passengerRepository;

    @Override
    public PagedResult<Passenger> findAllPassengers(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Passenger> passengerPage = passengerRepository.findAll(pageable);

        return new PagedResult<>(passengerPage);
    }

    @Override
    public Optional<Passenger> findPassengerById(Long id) {
        return passengerRepository.findById(id);
    }

    @Override
    public Optional<Passenger> findPassengerByFirstNameIgnoreCase(String firstName) {
        return passengerRepository.findByFirstNameIgnoreCase(firstName);
    }

    @Override
    public Passenger savePassenger(Passenger passenger) {
        passenger.setAge(passenger.getDateOfBirth());
        return passengerRepository.save(passenger);
    }

    @Override
    public void deletePassengerById(Long id) {
        passengerRepository.deleteById(id);
    }
}
