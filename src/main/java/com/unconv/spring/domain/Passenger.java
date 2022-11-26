package com.unconv.spring.domain;

import com.unconv.spring.consts.Gender;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    @NotNull(message = "Gender cannot be null")
    private Gender gender;
}
