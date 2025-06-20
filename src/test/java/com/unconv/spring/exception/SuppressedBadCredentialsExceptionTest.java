package com.unconv.spring.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

class SuppressedBadCredentialsExceptionTest {

    @Test
    void testMessageIsPreserved() {
        String message = "Invalid credentials";
        SuppressedBadCredentialsException exception =
                new SuppressedBadCredentialsException(message);

        assertEquals(
                message, exception.getMessage(), "Exception message should match input message");
        assertTrue(
                exception instanceof BadCredentialsException,
                "Exception should be a subclass of BadCredentialsException");
    }
}
