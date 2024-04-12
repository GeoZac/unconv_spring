package com.unconv.spring.exception;

import lombok.Getter;

@Getter
public class UnknownAuthTokenException extends SensorAuthTokenException {

    public UnknownAuthTokenException(String token) {
        super("Unknown API token", token);
    }
}
