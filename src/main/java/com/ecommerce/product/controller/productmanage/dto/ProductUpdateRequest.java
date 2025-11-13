package com.ecommerce.product.controller.productmanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating an existing product")
public record ProductUpdateRequest(

        @Schema(description = "ID of the *new* category", example = "2") @NotNull(message = "categoryId cannot be null") Long categoryId,

        @Schema(description = "Product title", example = "Super Game Pro (2025 Edition)") @NotBlank(message = "title cannot be blank") @Size(max = 255, message = "title max length is 255") String title,

        @Schema(description = "Product description", example = "The best game ever, now updated.") String description,

        @Schema(description = "Price in cents", example = "6999") @NotNull(message = "price cannot be null") @Min(value = 0, message = "price must be positive") Long price,

        @Schema(description = "Available stock quantity", example = "200") @NotNull(message = "stockAvailable cannot be null") @Min(value = 0, message = "stockAvailable must be positive") Integer stockAvailable) {
}
