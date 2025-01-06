package com.unconv.spring.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class SuppressedBadCredentialsException extends BadCredentialsException {
    public SuppressedBadCredentialsException(String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        // Suppress stack trace generation
        return this;
    }
}
