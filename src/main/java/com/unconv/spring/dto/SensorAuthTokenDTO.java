package com.unconv.spring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unconv.spring.domain.SensorSystem;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Data Transfer Object for SensorAuthToken entity */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorAuthTokenDTO {

    private UUID id;

    @NotEmpty(message = "Auth token cannot be empty")
    private String authToken;

    @Future(message = "Expiry has to be in future")
    @NotNull(message = "Expiry cannot be empty")
    private OffsetDateTime expiry;

    @NotNull(message = "Sensor system cannot be empty")
    private SensorSystem sensorSystem;
}
