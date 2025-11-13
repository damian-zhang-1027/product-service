package com.ecommerce.product.kafka.dto;

public record StockReserveFailedEvent(
        Long orderId,
        String reason) {
}
