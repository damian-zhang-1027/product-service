package com.ecommerce.product.kafka.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PaymentEventDto(
        @NotNull String eventType,
        @NotNull Long orderId,
        @NotNull @Valid List<OrderItemDto> items,
        @NotNull @Valid EventMetadata metadata) {
}
