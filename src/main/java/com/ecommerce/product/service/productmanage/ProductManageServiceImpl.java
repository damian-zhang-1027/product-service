package com.ecommerce.product.service.productmanage;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.controller.productmanage.dto.ProductCreateRequest;
import com.ecommerce.product.controller.productmanage.dto.ProductSellerResponse;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.model.db.entity.Category;
import com.ecommerce.product.model.db.entity.Product;
import com.ecommerce.product.repository.db.CategoryRepository;
import com.ecommerce.product.repository.db.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductManageServiceImpl implements ProductManageService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductSellerResponse createProduct(ProductCreateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long sellerAdminId = Long.parseLong(jwt.getSubject());

        log.info("Creating product '{}' for sellerAdminId: {}", request.title(), sellerAdminId);

        if (!categoryRepository.existsById(request.categoryId())) {
            log.warn("CategoryNotFoundException for categoryId: {}", request.categoryId());
            throw new CategoryNotFoundException(request.categoryId());
        }

        Product product = new Product();
        product.setSellerAdminId(sellerAdminId);
        product.setCategoryId(request.categoryId());
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockAvailable(request.stockAvailable());
        product.setStockReserved(0);

        Product savedProduct = productRepository.save(product);
        Category category = categoryRepository.getReferenceById(savedProduct.getCategoryId());
        savedProduct.setCategory(category);

        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return new ProductSellerResponse(savedProduct);
    }
}
