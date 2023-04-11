package com.unconv.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeaterDTO {

    private Long id;

    @NotNull(message = "Heater temperature cannot be empty")
    private Float temperature;

    @NotNull(message = "Heater temperature tolerance cannot be empty")
    private Float tempTolerance;
}
