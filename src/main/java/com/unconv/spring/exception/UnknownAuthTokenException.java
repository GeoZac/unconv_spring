package com.unconv.spring.exception;

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
        super("Unknown API token", token);
    }
}
