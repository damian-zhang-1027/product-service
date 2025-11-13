package com.ecommerce.product.service.productmanage;

import com.ecommerce.product.controller.productmanage.dto.ProductCreateRequest;
import com.ecommerce.product.controller.productmanage.dto.ProductSellerResponse;

public interface ProductManageService {

    ProductSellerResponse createProduct(ProductCreateRequest request);
}
