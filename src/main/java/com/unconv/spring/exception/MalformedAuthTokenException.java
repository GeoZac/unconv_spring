package com.unconv.spring.exception;

import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_MALFORMED;

import lombok.Getter;

/**
 * Exception indicating that an authentication token is malformed. This exception is a subclass of
 * {@code SensorAuthTokenException}.
 */
@Getter
public class MalformedAuthTokenException extends SensorAuthTokenException {

    /**
     * Constructs a new {@code MalformedAuthTokenException} with the specified token.
     *
     * @param token The malformed authentication token.
     */
    public MalformedAuthTokenException(String token) {
        super(SENS_AUTH_MALFORMED, token);
    }
}
