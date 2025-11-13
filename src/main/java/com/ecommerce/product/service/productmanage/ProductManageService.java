package com.ecommerce.product.service.productmanage;

import com.ecommerce.product.controller.productmanage.dto.ProductCreateRequest;
import com.ecommerce.product.controller.productmanage.dto.ProductSellerResponse;
import com.ecommerce.product.controller.productmanage.dto.ProductUpdateRequest;

public interface ProductManageService {

    ProductSellerResponse createProduct(ProductCreateRequest request);

    ProductSellerResponse updateProduct(Long productId, ProductUpdateRequest request);

    void deleteProduct(Long productId);
}
