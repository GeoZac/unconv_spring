package com.unconv.spring.dto;

import com.unconv.spring.domain.Passenger;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private Long id;

    @NotEmpty(message = "Booking Reference cannot be empty")
    private String bookingReference;

    private List<Passenger> passengers = new ArrayList<>();
}
