package com.ecommerce.product.controller.sellerbrowse.dto;

import com.ecommerce.product.model.db.entity.Category;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product category details (Seller context)")
public record CategoryResponse(
        @Schema(description = "Category ID", example = "1") Long id,
        @Schema(description = "Category Name", example = "GAMES") String name) {
    public CategoryResponse(Category entity) {
        this(entity.getId(), entity.getName());
    }
}
