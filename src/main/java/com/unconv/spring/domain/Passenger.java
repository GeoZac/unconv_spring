package com.unconv.spring.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.unconv.spring.enums.Gender;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.Period;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {
    public Passenger(
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

    public Passenger(
            Long id,
            String firstName,
            String middleName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            Booking booking) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.booking = booking;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotEmpty(message = "First name cannot be empty")
    private String firstName;

    @Column private String middleName;

    @Column(nullable = false)
    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;

    @Column(nullable = false)
    @Digits(fraction = 0, integer = 10, message = "Age must be a number and not greater than 200")
    @NotNull(message = "Age cannot be empty")
    private int age;

    @Column(nullable = false)
    @NotNull(message = "Date of Birth cannot be empty")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    @NotNull(message = "Gender cannot be null")
    private Gender gender;

    @JsonBackReference
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    public void setAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        this.age = Period.between(dateOfBirth, currentDate).getYears();
    }
}
