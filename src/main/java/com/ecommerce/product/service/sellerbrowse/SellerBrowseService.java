package com.ecommerce.product.service.sellerbrowse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.product.controller.sellerbrowse.dto.ProductSellerResponse;

public interface SellerBrowseService {

    Page<ProductSellerResponse> getMyProducts(Long categoryId, Pageable pageable);

    ProductSellerResponse getMyProductById(Long productId);
}
