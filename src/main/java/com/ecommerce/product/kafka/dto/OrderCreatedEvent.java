package com.ecommerce.product.kafka.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreatedEvent(
        @NotNull String eventType,

        @NotNull Long orderId,

        @NotNull @Positive Long totalAmount,

        @NotNull @NotEmpty @Valid List<OrderItemDto> items,

        @NotNull @Valid EventMetadata metadata) {
}
