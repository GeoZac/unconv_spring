package com.unconv.spring.service;

import com.unconv.spring.domain.Passenger;
import com.unconv.spring.persistence.PassengerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PassengerService {

    private final PassengerRepository passengerRepository;

    @Autowired
    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public List<Passenger> findAllPassengers() {
        return passengerRepository.findAll();
    }

    public Optional<Passenger> findPassengerById(Long id) {
        return passengerRepository.findById(id);
    }

    public Passenger savePassenger(Passenger passenger) {
        return passengerRepository.save(passenger);
    }

    public void deletePassengerById(Long id) {
        passengerRepository.deleteById(id);
    }
}
