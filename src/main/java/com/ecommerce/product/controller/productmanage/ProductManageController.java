package com.ecommerce.product.controller.productmanage;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.controller.productmanage.dto.ProductCreateRequest;
import com.ecommerce.product.controller.productmanage.dto.ProductSellerResponse;
import com.ecommerce.product.controller.productmanage.dto.ProductUpdateRequest;
import com.ecommerce.product.framework.response.GlobalResponse;
import com.ecommerce.product.service.productmanage.ProductManageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product (Seller Management API)", description = "APIs for sellers to manage their products. Requires ROLE_SELLER_ADMIN.")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProductManageController {

    private final ProductManageService productManageService;

    @Operation(summary = "Add a new product (Seller)", description = "Adds a new product for the currently authenticated seller. Requires ROLE_SELLER_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSellerResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., validation error, categoryId not found)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed (Invalid or missing token)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (User does not have ROLE_SELLER_ADMIN)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_SELLER_ADMIN')")
    public GlobalResponse<ProductSellerResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductSellerResponse responseData = productManageService.createProduct(request);
        return GlobalResponse.success(responseData);
    }

    @Operation(summary = "Update an existing product (Seller)", description = "Updates a product *owned* by the currently authenticated seller. Requires ROLE_SELLER_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSellerResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., categoryId not found)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed (Invalid or missing token)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (User does not own this product)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class)))
    })
    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_SELLER_ADMIN')")
    public GlobalResponse<ProductSellerResponse> updateProduct(
            @Parameter(description = "The ID of the product to update", example = "101") @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductSellerResponse responseData = productManageService.updateProduct(productId, request);
        return GlobalResponse.success(responseData);
    }

    @Operation(summary = "Delete a product (Seller)", description = "Deletes a product *owned* by the currently authenticated seller. Requires ROLE_SELLER_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeleteResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed (Invalid or missing token)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (User does not own this product)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class)))
    })
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_SELLER_ADMIN')")
    public GlobalResponse<Object> deleteProduct(
            @Parameter(description = "The ID of the product to delete", example = "101") @PathVariable Long productId) {
        productManageService.deleteProduct(productId);
        return GlobalResponse.success(Map.of("deletedId", productId));
    }

    @Schema(description = "Response wrapper for a Single Seller Product")
    private static class ProductSellerResponseWrapper {
        @Schema(example = "0")
        public int retCode;
        @Schema(description = "The product details (private view)")
        public ProductSellerResponse data;
        @Schema(nullable = true)
        public Object meta;
    }

    @Schema(description = "Response wrapper for a Delete operation")
    private static class DeleteResponseWrapper {
        @Schema(example = "0")
        public int retCode;
        @Schema(description = "Contains the ID of the deleted resource", example = "{\"deletedId\": 101}")
        public Object data;
        @Schema(nullable = true)
        public Object meta;
    }
}
