package com.ecommerce.product.kafka.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SagaEventPayload(
        @NotNull Long orderId,
        @NotNull @Positive Long totalAmount,
        @NotNull @NotEmpty @Valid List<OrderItemDto> items) {
}
