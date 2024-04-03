package com.unconv.spring.exception;

import java.time.OffsetDateTime;
import lombok.Getter;

@Getter
public class ExpiredAuthTokenException extends RuntimeException {

    private final String token;
    private final OffsetDateTime time;

    public ExpiredAuthTokenException(String token) {
        super("Expired API token");
        this.token = token;
        this.time = OffsetDateTime.now();
    }
}
