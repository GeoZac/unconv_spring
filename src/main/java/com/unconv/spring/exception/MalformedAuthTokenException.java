package com.unconv.spring.exception;

import lombok.Getter;

@Getter
public class MalformedAuthTokenException extends SensorAuthTokenException {

    public MalformedAuthTokenException(String token) {
        super("Malformed API token", token);
    }
}
