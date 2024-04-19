package com.unconv.spring.exception;

import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_MALFORMED;

import lombok.Getter;

@Getter
public class MalformedAuthTokenException extends SensorAuthTokenException {

    public MalformedAuthTokenException(String token) {
        super(SENS_AUTH_MALFORMED, token);
    }
}
