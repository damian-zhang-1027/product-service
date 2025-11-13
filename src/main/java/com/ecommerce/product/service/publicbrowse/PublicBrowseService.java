package com.ecommerce.product.service.publicbrowse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.product.controller.publicbrowse.dto.ProductPublicResponse;

public interface PublicBrowseService {

    Page<ProductPublicResponse> getAllProducts(Long categoryId, Pageable pageable);

    Page<ProductPublicResponse> searchProducts(String query, Pageable pageable);

    ProductPublicResponse getProductById(Long productId);
}
