package com.unconv.spring.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
