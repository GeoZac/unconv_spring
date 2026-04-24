package com.unconv.spring.exception;

import java.time.OffsetDateTime;
import lombok.Getter;

/**
 * Exception thrown when an error occurs related to sensor authentication tokens. This exception
 * extends {@link RuntimeException}, indicating that it is an unchecked exception.
 */
@Getter
public class SensorAuthTokenException extends RuntimeException {

    /** The authentication token associated with the exception. */
    private final String token;

    /** The timestamp when the exception occurred. */
    private final OffsetDateTime time;

    /**
     * Constructs a new SensorAuthTokenException with the specified detail message and token.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link
     *     #getMessage()} method)
     * @param token the authentication token associated with the exception
     */
    public SensorAuthTokenException(String message, String token) {
        super(message);
        this.token = token;
        this.time = OffsetDateTime.now();
        clipStackTrace();
    }

    protected void clipStackTrace() {
        StackTraceElement[] fullStackTrace = getStackTrace();
        if (fullStackTrace.length > 5) {
            StackTraceElement[] clipped = new StackTraceElement[5];
            System.arraycopy(fullStackTrace, 0, clipped, 0, 5);
            setStackTrace(clipped);
        }
    }
}
