package com.ecommerce.product.controller.productmanage;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.controller.productmanage.dto.ProductCreateRequest;
import com.ecommerce.product.controller.productmanage.dto.ProductSellerResponse;
import com.ecommerce.product.framework.response.GlobalResponse;
import com.ecommerce.product.service.productmanage.ProductManageService;

import io.swagger.v3.oas.annotations.Operation;
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

    @Schema(description = "Response wrapper for a Single Seller Product")
    private static class ProductSellerResponseWrapper {
        @Schema(example = "0")
        public int retCode;
        @Schema(description = "The product details (private view)")
        public ProductSellerResponse data;
        @Schema(nullable = true)
        public Object meta;
    }
}
