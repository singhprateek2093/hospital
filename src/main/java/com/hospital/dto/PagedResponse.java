package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * A consistent pagination envelope returned by all paginated list endpoints:
 *
 *   {
 *     "content": [ ... ],
 *     "page": 0, "size": 10,
 *     "total_elements": 42, "total_pages": 5,
 *     "first": true, "last": false
 *   }
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages,
        boolean first,
        boolean last
) {
    /** Build from a Spring Data Page, mapping each entity to a DTO. */
    public static <E, T> PagedResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
