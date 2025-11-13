package com.ecommerce.product.controller.productlookup.dto;

import java.time.Instant;

import com.ecommerce.product.model.db.entity.Product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public view of a product")
public record ProductPublicResponse(
        @Schema(description = "Product ID", example = "101") Long id,

        @Schema(description = "Seller ID (public)", example = "12") Long sellerAdminId,

        @Schema(description = "Category details") CategoryResponse category,

        @Schema(description = "Product title", example = "Super Game Pro") String title,

        @Schema(description = "Product description", example = "The best game ever.") String description,

        @Schema(description = "Price in cents (5999 = 59.99)", example = "5999") Long price,

        @Schema(description = "Available stock", example = "150") Integer stockAvailable,

        @Schema(description = "Last updated timestamp") Instant updatedAt) {
    public ProductPublicResponse(Product entity) {
        this(
                entity.getId(),
                entity.getSellerAdminId(),
                new CategoryResponse(entity.getCategory()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStockAvailable(),
                entity.getUpdatedAt());
    }
}
