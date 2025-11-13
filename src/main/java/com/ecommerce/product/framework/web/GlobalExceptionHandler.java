package com.ecommerce.product.framework.web;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecommerce.product.exception.CategoryNotFoundException;
import com.ecommerce.product.exception.ProductAccessDeniedException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.framework.response.GlobalResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Global Exception Handler.
 * Captures exceptions and returns the standard failure response format.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles 400 Bad Request (Bean Validation failures).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errorMessage);
        GlobalResponse<Object> response = GlobalResponse.error(errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<GlobalResponse<Object>> handleCategoryNotFoundException(CategoryNotFoundException ex) {
        log.warn("Invalid input: {}", ex.getMessage());
        GlobalResponse<Object> response = GlobalResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles 401 Unauthorized (Authentication failures).
     * This catches BadCredentialsException (wrong password)
     * and UsernameNotFoundException (wrong email) from the AuthenticationManager.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GlobalResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        GlobalResponse<Object> response = GlobalResponse.error("Authentication failed: Bad credentials");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ AccessDeniedException.class, ProductAccessDeniedException.class })
    public ResponseEntity<GlobalResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        GlobalResponse<Object> response = GlobalResponse.error("Access Denied");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles 404 Not Found (Product not found).
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<GlobalResponse<Object>> handleProductNotFoundException(ProductNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        GlobalResponse<Object> response = GlobalResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles 500 Internal Server Error (all other uncaught exceptions).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Object>> handleGeneralException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        GlobalResponse<Object> response = GlobalResponse.error("Internal server error.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
