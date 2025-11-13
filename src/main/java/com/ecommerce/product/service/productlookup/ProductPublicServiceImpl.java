package com.ecommerce.product.service.productlookup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.controller.productlookup.dto.ProductPublicResponse;
import com.ecommerce.product.model.db.entity.Product;
import com.ecommerce.product.repository.db.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPublicServiceImpl implements ProductPublicService {

    private final ProductRepository productRepository;

    @Override
    public Page<ProductPublicResponse> getAllProducts(Long categoryId, Pageable pageable) {
        log.info("Fetching public product list for categoryId: {} (Page: {}, Size: {})", categoryId,
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productPage;

        if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(ProductPublicResponse::new);
    }
}
