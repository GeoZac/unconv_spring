package com.unconv.spring.exception;

import java.time.OffsetDateTime;
import lombok.Getter;

@Getter
public class MalformedAuthTokenException extends RuntimeException {

    private final String token;
    private final OffsetDateTime time;

    public MalformedAuthTokenException(String token) {
        super("Malformed API token");
        this.token = token;
        this.time = OffsetDateTime.now();
    }
}
