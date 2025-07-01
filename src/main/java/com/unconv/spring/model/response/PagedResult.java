package com.unconv.spring.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * A generic class representing a paginated result.
 *
 * @param <T> The type of data contained in the paginated result.
 */
public record PagedResult<T>(
        /*
         The list of data elements for the current page.
        */
        List<T> data,

        /*
         * The total number of elements across all pages.
         */
        long totalElements,

        /*
         * The current page number.
         */
        int pageNumber,

        /*
         * The total number of pages.
         */
        int totalPages,

        /*
         * Indicates whether the current page is the first page.
         */
        @JsonProperty("isFirst") boolean isFirst,

        /*
         * Indicates whether the current page is the last page.
         */
        @JsonProperty("isLast") boolean isLast,

        /*
         * Indicates whether there is a next page.
         */
        @JsonProperty("hasNext") boolean hasNext,

        /*
         * Indicates whether there is a previous page.
         */
        @JsonProperty("hasPrevious") boolean hasPrevious) {

    /**
     * Constructs a {@code PagedResult} object from a Spring Data {@link Page}.
     *
     * @param page The Spring Data Page object containing data to populate the PagedResult.
     */
    public PagedResult(Page<T> page) {
        this(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious());
    }
}
