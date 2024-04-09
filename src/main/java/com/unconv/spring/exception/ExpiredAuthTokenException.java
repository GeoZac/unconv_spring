package com.unconv.spring.exception;

import lombok.Getter;

@Getter
public class ExpiredAuthTokenException extends SensorAuthTokenException {

    public ExpiredAuthTokenException(String token) {
        super("Expired API token", token);
    }
}
