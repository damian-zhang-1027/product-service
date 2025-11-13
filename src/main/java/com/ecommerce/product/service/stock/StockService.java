package com.ecommerce.product.service.stock;

import com.ecommerce.product.kafka.dto.OrderCreatedEvent;
import com.ecommerce.product.kafka.dto.PaymentEventDto;

public interface StockService {

    void processOrderCreated(OrderCreatedEvent event);

    void processPaymentSucceeded(PaymentEventDto event);

    void processPaymentFailed(PaymentEventDto event);
}
