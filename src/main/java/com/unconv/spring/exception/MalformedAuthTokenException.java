package com.unconv.spring.exception;

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
        super("Malformed API token", token);
    }
}
