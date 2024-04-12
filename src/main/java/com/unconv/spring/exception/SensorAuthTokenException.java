package com.unconv.spring.exception;

import java.time.OffsetDateTime;
import lombok.Getter;

@Getter
public class SensorAuthTokenException extends RuntimeException {
    private final String token;
    private final OffsetDateTime time;

    public SensorAuthTokenException(String message, String token) {
        super(message);
        this.token = token;
        this.time = OffsetDateTime.now();
    }
}
