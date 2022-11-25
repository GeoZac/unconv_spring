package com.unconv.spring.web.rest;

import com.unconv.spring.domain.Passenger;
import com.unconv.spring.service.PassengerService;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Passenger")
@Slf4j
public class PassengerController {

    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @GetMapping
    public List<Passenger> getAllPassengers() {
        return passengerService.findAllPassengers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Passenger> getPassengerById(@PathVariable Long id) {
        return passengerService
                .findPassengerById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Passenger createPassenger(@RequestBody @Validated Passenger passenger) {
        return passengerService.savePassenger(passenger);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Passenger> updatePassenger(
            @PathVariable Long id, @RequestBody Passenger passenger) {
        return passengerService
                .findPassengerById(id)
                .map(
                        passengerObj -> {
                            passenger.setId(id);
                            return ResponseEntity.ok(passengerService.savePassenger(passenger));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Passenger> deletePassenger(@PathVariable Long id) {
        return passengerService
                .findPassengerById(id)
                .map(
                        passenger -> {
                            passengerService.deletePassengerById(id);
                            return ResponseEntity.ok(passenger);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
