package com.ecommerce.product.service.productlookup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.product.controller.productlookup.dto.ProductPublicResponse;

public interface ProductPublicService {
    Page<ProductPublicResponse> getAllProducts(Long categoryId, Pageable pageable);
}
