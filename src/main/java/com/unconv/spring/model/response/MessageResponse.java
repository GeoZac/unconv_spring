package com.unconv.spring.model.response;

public record MessageResponse<T>(String message, T entity) {
    public MessageResponse(T entity, String message) {
        this(message, entity);
    }
}
