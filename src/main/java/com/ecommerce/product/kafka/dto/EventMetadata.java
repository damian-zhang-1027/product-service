package com.ecommerce.product.kafka.dto;

public record EventMetadata(
        String traceId,
        String causationId,
        String userId,
        Long timestamp) {
}
