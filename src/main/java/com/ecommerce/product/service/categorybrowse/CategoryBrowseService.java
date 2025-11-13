package com.ecommerce.product.service.categorybrowse;

import java.util.List;

import com.ecommerce.product.controller.categorybrowse.dto.CategoryResponse;

public interface CategoryBrowseService {
    List<CategoryResponse> getAllCategories();
}
