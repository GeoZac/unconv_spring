package com.unconv.spring.exception;

public class ReadingsCSVProcessingException extends RuntimeException {
    public ReadingsCSVProcessingException(String message) {
        super(message);
    }

    public ReadingsCSVProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
