package com.unconv.spring.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SensorAuthTokenExceptionTest {

    @Test
    void testStackTraceIsUnchangedWhenShortOrEqualToFive() {
        SensorAuthTokenException exception =
                new SensorAuthTokenException("Short stack trace", "random token") {
                    {
                        StackTraceElement[] shortTrace =
                                new StackTraceElement[] {
                                    new StackTraceElement("ClassX", "methodX", "ClassX.java", 10),
                                    new StackTraceElement("ClassY", "methodY", "ClassY.java", 20),
                                    new StackTraceElement("ClassZ", "methodZ", "ClassZ.java", 30)
                                };
                        setStackTrace(shortTrace);
                        clipStackTrace();
                    }
                };

        StackTraceElement[] result = exception.getStackTrace();
        assertEquals(3, result.length, "Stack trace should remain unchanged if length <= 5");
    }
}
