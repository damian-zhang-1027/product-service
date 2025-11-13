package com.ecommerce.product.controller.productlookup;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.controller.productlookup.dto.ProductPublicResponse;
import com.ecommerce.product.framework.response.GlobalResponse;
import com.ecommerce.product.framework.response.dto.PaginationMeta;
import com.ecommerce.product.service.productlookup.ProductPublicService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product (Public API)", description = "Public APIs for browsing and searching products")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductPublicController {

    private final ProductPublicService productPublicService;

    @Operation(summary = "Browse/Filter all products (Paginated)", description = "Get a paginated list of all products, optionally filtered by categoryId.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedProductResponseWrapper.class)))
    @GetMapping
    public GlobalResponse<List<ProductPublicResponse>> getAllProducts(
            @Parameter(description = "Filter by category ID", example = "1") @RequestParam(required = false) Long categoryId,
            @Parameter(hidden = true) @PageableDefault(size = 20, page = 0) Pageable pageable) {
        Page<ProductPublicResponse> productPage = productPublicService.getAllProducts(categoryId, pageable);
        PaginationMeta meta = new PaginationMeta(productPage);
        return GlobalResponse.success(productPage.getContent(), meta);
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
}
