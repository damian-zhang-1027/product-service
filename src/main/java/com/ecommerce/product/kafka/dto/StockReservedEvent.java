package com.ecommerce.product.kafka.dto;

import java.util.List;

public record StockReservedEvent(
        Long orderId,

        Long totalAmount,

        List<OrderItemDto> items) {
}
