package com.ecommerce.product.framework.response.dto;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pagination metadata")
public record PaginationMeta(
        @Schema(description = "Total number of items", example = "50") long totalElements,

        @Schema(description = "Total number of pages", example = "5") int totalPages,

        @Schema(description = "Current page number (0-indexed)", example = "0") int pageNumber,

        @Schema(description = "Number of items per page", example = "10") int pageSize) {
    public PaginationMeta(Page<?> page) {
        this(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }
}
