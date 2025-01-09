package com.unconv.spring.exception;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * A custom exception that extends {@link BadCredentialsException} and suppresses the generation of
 * stack traces for performance reasons.
 *
 * <p>This exception is useful in scenarios where stack trace generation and logging are
 * unnecessary, such as in cases of frequent authentication failures, to reduce overhead and improve
 * application performance.
 */
public class SuppressedBadCredentialsException extends BadCredentialsException {

    /**
     * Constructs a new {@code SuppressedBadCredentialsException} with the specified detail message.
     *
     * @param message the detail message, which provides information about the exception.
     */
    public SuppressedBadCredentialsException(String message) {
        super(message);
    }

    /**
     * Overrides {@link Throwable#fillInStackTrace()} to suppress the generation of a stack trace.
     *
     * @return this instance, with no stack trace generated.
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        // Suppress stack trace generation
        return this;
    }
}
