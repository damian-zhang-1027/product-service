package com.ecommerce.product.service.publicbrowse;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.controller.publicbrowse.dto.ProductPublicResponse;
import com.ecommerce.product.model.db.entity.Product;
import com.ecommerce.product.repository.db.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicBrowseServiceImpl implements PublicBrowseService {

    private final ProductRepository productRepository;

    @Override
    public Page<ProductPublicResponse> getAllProducts(Long categoryId, Pageable pageable) {
        log.info("Fetching public product list for categoryId: {} (Page: {}, Size: {})",
                categoryId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productPage;

        if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(ProductPublicResponse::new);
    }

    @Override
    public Page<ProductPublicResponse> searchProducts(String query, Pageable pageable) {

        String formattedQuery = formatFullTextQuery(query);

        log.info("Executing full-text search for query: '{}' (Formatted: '{}')", query, formattedQuery);

        Page<Product> productPage = productRepository.searchByTitleAndDescription(formattedQuery, pageable);

        return productPage.map(ProductPublicResponse::new);
    }

    private String formatFullTextQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        return Arrays.stream(query.trim().split("\\s+"))
                .filter(term -> !term.isBlank())
                .map(term -> "+" + term + "*")
                .collect(Collectors.joining(" "));
    }
}
