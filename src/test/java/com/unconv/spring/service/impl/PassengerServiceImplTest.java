package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.Passenger;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.PassengerRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PassengerServiceImplTest {

    @Mock private PassengerRepository passengerRepository;

    @InjectMocks private PassengerServiceImpl passengerService;

    private Passenger passenger;
    private Long passengerId;

    @BeforeEach
    void setUp() {
        passengerId = 1L;
        passenger = new Passenger();
        passenger.setId(passengerId);
    }

    @Test
    void findAllPassengers() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<Passenger> passengerList = Collections.singletonList(passenger);
        Page<Passenger> passengerPage = new PageImpl<>(passengerList);

        when(passengerRepository.findAll(any(Pageable.class))).thenReturn(passengerPage);

        PagedResult<Passenger> result =
                passengerService.findAllPassengers(pageNo, pageSize, sortBy, sortDir);

        assertEquals(passengerList.size(), result.data().size());
        assertEquals(passengerList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findPassengerById() {
        when(passengerRepository.findById(any(Long.class))).thenReturn(Optional.of(passenger));

        Optional<Passenger> result = passengerService.findPassengerById(passengerId);

        assertTrue(result.isPresent());
        assertEquals(passenger.getId(), result.get().getId());
    }

    @Test
    void findPassengerByFirstNameIgnoreCase() {}

    @Test
    void savePassenger() {
        passenger.setDateOfBirth(LocalDate.of(1990, 1, 1));
        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);

        Passenger result = passengerService.savePassenger(passenger);

        assertEquals(passenger.getId(), result.getId());
    }

    @Test
    void deletePassengerById() {
        passengerService.deletePassengerById(passengerId);

        verify(passengerRepository, times(1)).deleteById(passengerId);
    }
}
