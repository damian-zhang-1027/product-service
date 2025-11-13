package com.ecommerce.product.controller.productmanage.dto;

import java.time.Instant;

import com.ecommerce.product.model.db.entity.Product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Private view of a product (for sellers)")
public record ProductSellerResponse(
        @Schema(description = "Product ID", example = "101") Long id,

        @Schema(description = "Seller ID (The owner)", example = "12") Long sellerAdminId,

        @Schema(description = "Category details") CategoryResponse category,

        @Schema(description = "Product title", example = "Super Game Pro") String title,

        @Schema(description = "Product description", example = "The best game ever.") String description,

        @Schema(description = "Price in cents (5999 = 59.99)", example = "5999") Long price,

        @Schema(description = "Available stock", example = "150") Integer stockAvailable,

        @Schema(description = "Reserved stock (in pending orders)", example = "10") Integer stockReserved,

        @Schema(description = "Created timestamp") Instant createdAt,

        @Schema(description = "Last updated timestamp") Instant updatedAt) {
    public ProductSellerResponse(Product entity) {
        this(
                entity.getId(),
                entity.getSellerAdminId(),
                new CategoryResponse(entity.getCategory()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStockAvailable(),
                entity.getStockReserved(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
