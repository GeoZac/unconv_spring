package com.unconv.spring.exception;

/**
 * Exception thrown when an error occurs during the processing of readings from a CSV file.
 *
 * <p>This is a runtime exception that extends {@link RuntimeException}, allowing it to be thrown
 * without being explicitly declared in method signatures.
 */
public class ReadingsCSVProcessingException extends RuntimeException {
    public ReadingsCSVProcessingException(String message) {
        super(message);
    }
}
