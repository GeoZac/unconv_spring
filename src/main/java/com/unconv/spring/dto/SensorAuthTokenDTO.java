package com.unconv.spring.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.validation.constraints.Future;
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
public class SensorAuthTokenDTO {

    private UUID id;

    @NotEmpty(message = "Auth token cannot be empty")
    private String authToken;

    @Future(message = "Expiry has to be in future")
    @NotNull(message = "Expiry cannot be empty")
    private OffsetDateTime expiry;
}
