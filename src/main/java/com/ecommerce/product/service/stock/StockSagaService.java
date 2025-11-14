package com.ecommerce.product.service.stock;

import com.ecommerce.product.model.db.entity.OutboxEvent;

public interface StockSagaService {
    void processOrderCreated(OutboxEvent incomingEvent);
}
