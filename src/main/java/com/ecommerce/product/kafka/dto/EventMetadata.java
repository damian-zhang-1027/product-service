package com.ecommerce.product.kafka.dto;

import lombok.Builder;

@Builder
public record EventMetadata(
                String traceId,
                String causationId,
                String userId,
                Long timestamp) {
}
