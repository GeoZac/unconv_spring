package com.unconv.spring.exception;

/**
 * Exception thrown when an error occurs during the processing of readings from a CSV file.
 *
 * <p>This is a runtime exception that extends {@link RuntimeException}, allowing it to be thrown
 * without being explicitly declared in method signatures.
 */
public class ReadingsCSVProcessingException extends RuntimeException {

    /**
     * Constructs a new ReadingsCSVProcessingException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ReadingsCSVProcessingException(String message) {
        super(message);
    }
}
