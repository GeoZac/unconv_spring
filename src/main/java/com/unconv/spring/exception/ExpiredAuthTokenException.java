package com.unconv.spring.exception;

import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_EXPIRED;

import lombok.Getter;

/**
 * Exception indicating that an authentication token has expired. This exception is a subclass of
 * {@code SensorAuthTokenException}.
 */
@Getter
public class ExpiredAuthTokenException extends SensorAuthTokenException {

    /**
     * Constructs a new {@code ExpiredAuthTokenException} with the specified token.
     *
     * @param token The expired authentication token.
     */
    public ExpiredAuthTokenException(String token) {
        super(SENS_AUTH_EXPIRED, token);
    }
}
