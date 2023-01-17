package com.unconv.spring.web.rest;

import com.unconv.spring.domain.Passenger;
import com.unconv.spring.dto.PassengerDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.PassengerService;
import com.unconv.spring.utils.AppConstants;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
@RequestMapping("/Passenger")
@Slf4j
public class PassengerController {

    @Autowired private PassengerService passengerService;

    @Autowired private ModelMapper modelMapper;

    @GetMapping
    public PagedResult<Passenger> getAllPassengers(
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
        return passengerService.findAllPassengers(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Passenger> getPassengerById(@PathVariable Long id) {
        return passengerService
                .findPassengerById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("search/firstName/{firstName}")
    public ResponseEntity<Passenger> getPassengerByFirstNameIgnoreCase(
            @PathVariable String firstName) {
        return passengerService
                .findPassengerByFirstNameIgnoreCase(firstName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Passenger createPassenger(@RequestBody @Validated PassengerDTO passengerDTO) {
        return passengerService.savePassenger(modelMapper.map(passengerDTO, Passenger.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Passenger> updatePassenger(
            @PathVariable Long id, @RequestBody @Valid PassengerDTO passengerDTO) {
        return passengerService
                .findPassengerById(id)
                .map(
                        passengerObj -> {
                            passengerDTO.setId(id);
                            return ResponseEntity.ok(
                                    passengerService.savePassenger(
                                            modelMapper.map(passengerDTO, Passenger.class)));
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
