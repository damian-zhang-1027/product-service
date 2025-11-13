package com.ecommerce.product.service.productmanage;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.controller.productmanage.dto.ProductCreateRequest;
import com.ecommerce.product.controller.productmanage.dto.ProductSellerResponse;
import com.ecommerce.product.controller.productmanage.dto.ProductUpdateRequest;
import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.exception.ProductAccessDeniedException;
import com.ecommerce.product.exception.ProductNotFoundException;
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
        Long sellerAdminId = getAuthenticatedSellerId();
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

    @Override
    public ProductSellerResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Long sellerAdminId = getAuthenticatedSellerId();
        log.info("Updating product ID: {} by sellerAdminId: {}", productId, sellerAdminId);

        if (!categoryRepository.existsById(request.categoryId())) {
            log.warn("CategoryNotFoundException for categoryId: {}", request.categoryId());
            throw new CategoryNotFoundException(request.categoryId());
        }

        Product product = getProductAndVerifyOwnership(productId, sellerAdminId);
        product.setCategoryId(request.categoryId());
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockAvailable(request.stockAvailable());

        Product updatedProduct = productRepository.save(product);
        Category category = categoryRepository.getReferenceById(updatedProduct.getCategoryId());
        updatedProduct.setCategory(category);

        log.info("Product ID: {} updated successfully by sellerAdminId: {}", productId, sellerAdminId);
        return new ProductSellerResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long productId) {
        Long sellerAdminId = getAuthenticatedSellerId();
        log.info("Deleting product ID: {} by sellerAdminId: {}", productId, sellerAdminId);

        Product product = getProductAndVerifyOwnership(productId, sellerAdminId);

        productRepository.delete(product);

        log.info("Product ID: {} deleted successfully by sellerAdminId: {}", productId, sellerAdminId);
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
            log.warn("ProductAccessDeniedException: SellerId {} attempted to modify productId {} owned by {}",
                    sellerAdminId, productId, product.getSellerAdminId());
            throw new ProductAccessDeniedException("You do not have permission to modify this product.");
        }

        return product;
    }
}
