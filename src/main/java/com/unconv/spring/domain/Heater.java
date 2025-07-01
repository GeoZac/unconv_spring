package com.unconv.spring.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "heaters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Heater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "Heater temperature cannot be empty")
    private Float temperature;

    @Column(nullable = false)
    @NotNull(message = "Heater temperature tolerance cannot be empty")
    private Float tempTolerance;
}
