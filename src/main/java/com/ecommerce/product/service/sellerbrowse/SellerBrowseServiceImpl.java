package com.ecommerce.product.service.sellerbrowse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.controller.sellerbrowse.dto.ProductSellerResponse;
import com.ecommerce.product.exception.ProductAccessDeniedException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.db.entity.Product;
import com.ecommerce.product.repository.db.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerBrowseServiceImpl implements SellerBrowseService {

    private final ProductRepository productRepository;

    @Override
    public Page<ProductSellerResponse> getMyProducts(Long categoryId, Pageable pageable) {
        Long sellerAdminId = getAuthenticatedSellerId();
        log.info("Fetching seller's product list for sellerAdminId: {} (Page: {}, Size: {})",
                sellerAdminId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productPage;

        if (categoryId != null) {
            productPage = productRepository.findBySellerAdminIdAndCategoryId(sellerAdminId, categoryId, pageable);
        } else {
            productPage = productRepository.findBySellerAdminId(sellerAdminId, pageable);
        }

        return productPage.map(ProductSellerResponse::new);
    }

    @Override
    public ProductSellerResponse getMyProductById(Long productId) {
        Long sellerAdminId = getAuthenticatedSellerId();
        log.info("Fetching seller's product detail for sellerAdminId: {}, productId: {}",
                sellerAdminId, productId);

        Product product = getProductAndVerifyOwnership(productId, sellerAdminId);

        return new ProductSellerResponse(product);
    }

    private Long getAuthenticatedSellerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return Long.parseLong(jwt.getSubject());
    }

    private Product getProductAndVerifyOwnership(Long productId, Long sellerAdminId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("ProductNotFoundException for productId: {}", productId);
                    return new ProductNotFoundException(productId);
                });

        if (!product.getSellerAdminId().equals(sellerAdminId)) {
            log.warn("ProductAccessDeniedException: SellerId {} attempted to access productId {} owned by {}",
                    sellerAdminId, productId, product.getSellerAdminId());
            throw new ProductAccessDeniedException("You do not have permission to access this product.");
        }

        return product;
    }
}
