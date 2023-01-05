package com.unconv.spring.dto;

import com.unconv.spring.consts.Gender;
import java.time.LocalDate;
import java.time.Period;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDTO {
    public PassengerDTO(
            Long id,
            String firstName,
            String middleName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

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

    public void setAge(LocalDate dateOfBirth) {
        LocalDate currentDate = java.time.LocalDate.now();
        this.age = Period.between(dateOfBirth, currentDate).getYears();
    }
}
