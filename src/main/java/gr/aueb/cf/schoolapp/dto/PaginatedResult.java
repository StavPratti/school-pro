package gr.aueb.cf.schoolapp.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PaginatedResult<T> (
    List<T> data,
    int currentPage,
    int pageSize,
    int totalPages,
    long totalItems
){}
