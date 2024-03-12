package com.unconv.spring.exception;

public class MalformedAuthTokenException extends RuntimeException {

    public MalformedAuthTokenException(String message) {
        super(message);
    }
}
