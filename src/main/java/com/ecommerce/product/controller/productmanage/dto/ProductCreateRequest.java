package com.ecommerce.product.controller.productmanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new product")
public record ProductCreateRequest(

        @Schema(description = "ID of the category this product belongs to", example = "1") @NotNull(message = "categoryId cannot be null") Long categoryId,

        @Schema(description = "Product title", example = "Super Game Pro") @NotBlank(message = "title cannot be blank") @Size(max = 255, message = "title max length is 255") String title,

        @Schema(description = "Product description", example = "The best game ever.") String description,

        @Schema(description = "Price in cents (e.g., 5999 = 59.99)", example = "5999") @NotNull(message = "price cannot be null") @Min(value = 0, message = "price must be positive") Long price,

        @Schema(description = "Available stock quantity", example = "150") @NotNull(message = "stockAvailable cannot be null") @Min(value = 0, message = "stockAvailable must be positive") Integer stockAvailable) {
}
