package com.unconv.spring.exception;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * A custom exception that extends {@link BadCredentialsException} and suppresses the generation of
 * full stack traces for performance reasons.
 *
 * <p>This exception is particularly useful in high-throughput authentication systems where frequent
 * authentication failures are expected and stack trace generation is unnecessary. Limiting the
 * stack trace helps reduce CPU and memory overhead, especially when logging is involved.
 */
public class SuppressedBadCredentialsException extends BadCredentialsException {

    /**
     * Constructs a new {@code SuppressedBadCredentialsException} with the specified detail message.
     * The stack trace is truncated to a limited depth to reduce overhead.
     *
     * @param message the detail message providing context about the authentication failure.
     */
    public SuppressedBadCredentialsException(String message) {
        super(message);
        clipStackTrace();
    }

    /**
     * Truncates the stack trace to a maximum of 5 elements.
     *
     * <p>This method reduces the size of the stack trace associated with this exception, which can
     * be beneficial for performance in scenarios where detailed stack traces are not necessary.
     */
    private void clipStackTrace() {
        StackTraceElement[] fullStackTrace = getStackTrace();
        if (fullStackTrace.length > 5) {
            StackTraceElement[] clipped = new StackTraceElement[5];
            System.arraycopy(fullStackTrace, 0, clipped, 0, 5);
            setStackTrace(clipped);
        }
    }
}
