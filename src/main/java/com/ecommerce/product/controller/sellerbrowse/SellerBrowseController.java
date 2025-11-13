package com.ecommerce.product.controller.sellerbrowse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.controller.sellerbrowse.dto.ProductSellerResponse;
import com.ecommerce.product.framework.response.GlobalResponse;
import com.ecommerce.product.framework.response.dto.PaginationMeta;
import com.ecommerce.product.service.sellerbrowse.SellerBrowseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product (Seller Browse API)", description = "APIs for sellers to *browse* their own products. Requires ROLE_SELLER_ADMIN.")
@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ROLE_SELLER_ADMIN')")
public class SellerBrowseController {

    private final SellerBrowseService sellerBrowseService;

    @Operation(summary = "Get my products (Paginated)", description = "Get a paginated list of products *owned* by the currently authenticated seller.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedProductSellerResponseWrapper.class)))
    @GetMapping
    public GlobalResponse<List<ProductSellerResponse>> getMyProducts(
            @Parameter(description = "Filter by category ID", example = "1") @RequestParam(required = false) Long categoryId,
            @Parameter(hidden = true) @PageableDefault(size = 20, page = 0) Pageable pageable) {
        Page<ProductSellerResponse> productPage = sellerBrowseService.getMyProducts(categoryId, pageable);
        PaginationMeta meta = new PaginationMeta(productPage);
        return GlobalResponse.success(productPage.getContent(), meta);
    }

    @Operation(summary = "Get my single product's details", description = "Get details for a *specific* product *owned* by the currently authenticated seller.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSellerResponseWrapper.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (User does not own this product)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class)))
    })
    @GetMapping("/{productId}")
    public GlobalResponse<ProductSellerResponse> getMyProductById(
            @Parameter(description = "The ID of the product to retrieve", example = "101") @PathVariable Long productId) {
        ProductSellerResponse product = sellerBrowseService.getMyProductById(productId);
        return GlobalResponse.success(product);
    }

    @Schema(description = "Paginated response wrapper for Seller Products")
    private static class PaginatedProductSellerResponseWrapper {
        @Schema(example = "0")
        public int retCode;
        @Schema(description = "The list of seller's products for the current page")
        public List<ProductSellerResponse> data;
        @Schema(description = "Pagination metadata")
        public PaginationMeta meta;
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
