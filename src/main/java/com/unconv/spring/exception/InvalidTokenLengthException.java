package com.unconv.spring.exception;

import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_SHORT;

public class InvalidTokenLengthException extends SensorAuthTokenException {
    public InvalidTokenLengthException(String token) {
        super(SENS_AUTH_SHORT, token);
    }
}
