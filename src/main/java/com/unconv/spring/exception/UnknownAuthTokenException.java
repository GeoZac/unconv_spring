package com.unconv.spring.exception;

import java.time.OffsetDateTime;
import lombok.Getter;

@Getter
public class UnknownAuthTokenException extends RuntimeException {

    private final String token;
    private final OffsetDateTime time;

    public UnknownAuthTokenException(String token) {
        super("Unknown API token");
        this.token = token;
        this.time = OffsetDateTime.now();
    }
}
