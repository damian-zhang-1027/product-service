package com.ecommerce.product.controller.publicbrowse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.controller.publicbrowse.dto.ProductPublicResponse;
import com.ecommerce.product.framework.response.GlobalResponse;
import com.ecommerce.product.framework.response.dto.PaginationMeta;
import com.ecommerce.product.service.publicbrowse.PublicBrowseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product (Public API)", description = "Public APIs for browsing and searching products")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class PublicBrowseController {

    private final PublicBrowseService publicBrowseService;

    @Operation(summary = "Browse/Filter all products (Paginated)", description = "Get a paginated list of all products, optionally filtered by categoryId.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedProductResponseWrapper.class)))
    @GetMapping
    public GlobalResponse<List<ProductPublicResponse>> getAllProducts(
            @Parameter(description = "Filter by category ID", example = "1") @RequestParam(required = false) Long categoryId,
            @Parameter(hidden = true) @PageableDefault(size = 20, page = 0) Pageable pageable) {
        Page<ProductPublicResponse> productPage = publicBrowseService.getAllProducts(categoryId, pageable);
        PaginationMeta meta = new PaginationMeta(productPage);
        return GlobalResponse.success(productPage.getContent(), meta);
    }

    @Operation(summary = "Search products (Full-Text)", description = "Search products by title and description using full-text search.")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedProductResponseWrapper.class)))
    @GetMapping("/search")
    public GlobalResponse<List<ProductPublicResponse>> searchProducts(
            @Parameter(description = "The search query term (e.g., 'Super Game')", example = "Super Game") @RequestParam("q") String query,
            @Parameter(hidden = true) @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<ProductPublicResponse> productPage = publicBrowseService.searchProducts(query, pageable);
        PaginationMeta meta = new PaginationMeta(productPage);
        return GlobalResponse.success(productPage.getContent(), meta);
    }

    @Operation(summary = "Get a single product's public details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalResponse.class)))
    })
    @GetMapping("/{productId}")
    public GlobalResponse<ProductPublicResponse> getProductById(
            @Parameter(description = "The ID of the product", example = "101") @PathVariable Long productId) {
        ProductPublicResponse product = publicBrowseService.getProductById(productId);
        return GlobalResponse.success(product);
    }

    /**
     * swagger doc helper class for pagination response
     * since java type erasure and swagger's limitation on generic record,
     * we need this wrapper class to help swagger correctly generate api
     * documentation.
     *
     * note: GlobalResponse should be kept as record is the correct design choice,
     * because it is an immutable data body. This wrapper is only used for
     * documentation generation.
     */
    @Schema(description = "Paginated response wrapper for Public Products")
    private static class PaginatedProductResponseWrapper {
        @Schema(description = "Business status code (0=success, -1=failure)", example = "0")
        public int retCode;

        @Schema(description = "The list of products for the current page")
        public List<ProductPublicResponse> data;

        @Schema(description = "Pagination metadata")
        public PaginationMeta meta;
    }

    @Schema(description = "Response wrapper for a Single Public Product")
    private static class ProductResponseWrapper {
        @Schema(example = "0")
        public int retCode;
        @Schema(description = "The product details")
        public ProductPublicResponse data;
        @Schema(nullable = true)
        public Object meta;
    }
}
