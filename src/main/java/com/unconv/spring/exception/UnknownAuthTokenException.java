package com.unconv.spring.exception;

import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_UNKNOWN;

import lombok.Getter;

@Getter
public class UnknownAuthTokenException extends SensorAuthTokenException {

    public UnknownAuthTokenException(String token) {
        super(SENS_AUTH_UNKNOWN, token);
    }
}
