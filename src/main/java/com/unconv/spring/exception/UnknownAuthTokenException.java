package com.unconv.spring.exception;

public class UnknownAuthTokenException extends RuntimeException {

    public UnknownAuthTokenException(String message) {
        super(message);
    }
}
