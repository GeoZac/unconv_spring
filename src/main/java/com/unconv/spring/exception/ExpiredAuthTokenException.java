package com.unconv.spring.exception;

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
        super("Expired API token", token);
    }
}
