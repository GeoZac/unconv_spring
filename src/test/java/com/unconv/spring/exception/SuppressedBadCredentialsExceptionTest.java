package com.unconv.spring.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SuppressedBadCredentialsExceptionTest {

    @Test
    void testMessageIsPreserved() {
        String message = "Invalid credentials";
        SuppressedBadCredentialsException exception =
                new SuppressedBadCredentialsException(message);

        assertEquals(
                message, exception.getMessage(), "Exception message should match input message");
        assertTrue(true, "Exception should be a subclass of BadCredentialsException");
    }

    @Test
    void testStackTraceIsUnchangedWhenShortOrEqualToFive() {
        SuppressedBadCredentialsException exception =
                new SuppressedBadCredentialsException("Short stack trace") {
                    {
                        // Manually set a short stack trace (e.g., 3 elements)
                        StackTraceElement[] shortTrace =
                                new StackTraceElement[] {
                                    new StackTraceElement("ClassA", "methodA", "ClassA.java", 10),
                                    new StackTraceElement("ClassB", "methodB", "ClassB.java", 20),
                                    new StackTraceElement("ClassC", "methodC", "ClassC.java", 30)
                                };
                        setStackTrace(shortTrace);
                        clipStackTrace();
                    }
                };

        StackTraceElement[] result = exception.getStackTrace();
        assertEquals(3, result.length, "Stack trace should remain unchanged if length <= 5");
    }
}
