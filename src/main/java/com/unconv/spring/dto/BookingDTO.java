package com.unconv.spring.dto;

import com.unconv.spring.domain.Passenger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private Long id;

    @NotEmpty(message = "Booking cannot be empty")
    private String booking;

    private List<Passenger> passengers = new ArrayList<>();
}
