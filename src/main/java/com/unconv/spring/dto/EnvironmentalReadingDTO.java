package com.unconv.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalReadingDTO {

    private UUID id;

    @NotNull(message = "Temperature cannot be empty")
    private double temperature;

    @NotNull(message = "Humidity cannot be empty")
    private double humidity;

    @NotNull(message = "Timestamp cannot be empty")
    private OffsetDateTime timestamp;
}
