package com.unconv.spring.dto;

import com.unconv.spring.domain.Booking;
import com.unconv.spring.enums.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Period;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDTO {

    private Long id;

    @NotEmpty(message = "First name cannot be empty")
    private String firstName;

    private String middleName;

    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;

    @Digits(fraction = 0, integer = 10, message = "Age must be a number and not greater than 200")
    @NotNull(message = "Age cannot be empty")
    private int age;

    @NotNull(message = "Date of Birth cannot be empty")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.ORDINAL)
    @NotNull(message = "Gender cannot be null")
    private Gender gender;

    private Booking booking;

    public void setAge(LocalDate dateOfBirth) {
        LocalDate currentDate = java.time.LocalDate.now();
        this.age = Period.between(dateOfBirth, currentDate).getYears();
    }
}
