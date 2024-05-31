package com.unconv.spring.model.response;

/**
 * A generic class representing a response message with an associated entity.
 *
 * @param <T> The type of entity associated with the response.
 */
public record MessageResponse<T>(String message, T entity) {

    /**
     * Constructs a {@code MessageResponse} object with the specified entity and message.
     *
     * @param entity The entity associated with the response.
     * @param message The message associated with the response.
     */
    public MessageResponse(T entity, String message) {
        this(message, entity);
    }
}
