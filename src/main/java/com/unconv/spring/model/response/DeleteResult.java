package com.unconv.spring.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteResult<T> {
    T data;

    @JsonProperty("wasDeleted")
    boolean wasDeleted;

    public DeleteResult(T data, boolean wasDeleted) {
        this.data = data;
        this.wasDeleted = wasDeleted;
    }
}
