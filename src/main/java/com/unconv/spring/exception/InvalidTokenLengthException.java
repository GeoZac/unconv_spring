package com.unconv.spring.exception;

import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_SHORT;

/**
 * Exception thrown when the length of an access token is insufficient. This exception extends
 * {@link SensorAuthTokenException}, indicating a problem with the access token.
 */
public class InvalidTokenLengthException extends SensorAuthTokenException {

    /**
     * Constructs a new InvalidTokenLengthException with the specified token.
     *
     * @param token The access token that triggered this exception.
     */
    public InvalidTokenLengthException(String token) {
        super(SENS_AUTH_SHORT, token);
    }
}
