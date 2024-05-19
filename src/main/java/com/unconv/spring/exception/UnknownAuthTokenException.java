package com.unconv.spring.exception;

import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_UNKNOWN;

import lombok.Getter;

/**
 * Exception thrown when an unknown sensor authentication token is encountered. Extends {@link
 * SensorAuthTokenException}.
 */
@Getter
public class UnknownAuthTokenException extends SensorAuthTokenException {

    /**
     * Constructs a new UnknownAuthTokenException with the specified token.
     *
     * @param token The unknown authentication token causing the exception.
     */
    public UnknownAuthTokenException(String token) {
        super(SENS_AUTH_UNKNOWN, token);
    }
}
