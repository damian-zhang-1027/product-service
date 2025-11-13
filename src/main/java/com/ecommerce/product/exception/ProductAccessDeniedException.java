package com.ecommerce.product.exception;

import org.springframework.security.access.AccessDeniedException;

public class ProductAccessDeniedException extends AccessDeniedException {

    public ProductAccessDeniedException(String message) {
        super(message);
    }
}
