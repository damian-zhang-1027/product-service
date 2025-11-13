package com.ecommerce.product.controller.categorybrowse;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.controller.categorybrowse.dto.CategoryResponse;
import com.ecommerce.product.framework.response.GlobalResponse;
import com.ecommerce.product.service.categorybrowse.CategoryBrowseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Category (Public API)", description = "Public API for browsing categories")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryBrowseController {

    private final CategoryBrowseService categoryBrowseService;

    @Operation(summary = "Get all product categories", description = "Get a list of all product categories (e.g., for filtering menus).")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryListResponseWrapper.class)))
    @GetMapping
    public GlobalResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryBrowseService.getAllCategories();
        return GlobalResponse.success(categories);
    }

    @Schema(description = "Response wrapper for a List of Categories")
    private static class CategoryListResponseWrapper {
        @Schema(example = "0")
        public int retCode;
        @Schema(description = "The list of categories")
        public List<CategoryResponse> data;
        @Schema(nullable = true)
        public Object meta;
    }
}
