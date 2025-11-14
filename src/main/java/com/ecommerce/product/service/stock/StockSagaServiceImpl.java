package com.ecommerce.product.service.stock;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.kafka.dto.EventMetadata;
import com.ecommerce.product.kafka.dto.OrderItemDto;
import com.ecommerce.product.kafka.dto.SagaEventPayload;
import com.ecommerce.product.model.db.entity.OutboxEvent;
import com.ecommerce.product.model.db.entity.Product;
import com.ecommerce.product.repository.db.OutboxEventRepository;
import com.ecommerce.product.repository.db.ProductRepository;
import com.ecommerce.product.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockSagaServiceImpl implements StockSagaService {

    private final ProductRepository productRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final JsonUtil jsonUtil;

    private static final String TOPIC_STOCKS = "stocks";
    private static final String STATUS_PENDING = "PENDING";
    private static final String EVENT_TYPE_STOCK_RESERVED = "STOCK_RESERVED";
    private static final String EVENT_TYPE_STOCK_RESERVE_FAILED = "STOCK_RESERVE_FAILED";
    private static final String EVENT_TYPE_PAYMENT_SUCCEEDED = "PAYMENT_SUCCEEDED";
    private static final String EVENT_TYPE_PAYMENT_FAILED = "PAYMENT_FAILED";

    @Override
    @Transactional
    public void processOrderCreated(OutboxEvent incomingEvent) {
        SagaEventPayload payload = jsonUtil.fromJson(incomingEvent.getPayload(), SagaEventPayload.class);
        EventMetadata metadata = jsonUtil.fromJson(incomingEvent.getMetadata(), EventMetadata.class);

        List<Long> productIds = payload.items().stream()
                .map(OrderItemDto::productId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (OrderItemDto item : payload.items()) {
            Product product = productMap.get(item.productId());

            if (product == null) {
                log.warn("[Saga] Stock check failed. ProductId: {} not found.", item.productId());
                createOutboxEvent(payload, metadata, incomingEvent.getEventId(), EVENT_TYPE_STOCK_RESERVE_FAILED);
                return;
            }

            if (product.getStockAvailable() < item.quantity()) {
                log.warn("[Saga] Stock check failed. Insufficient stock for ProductId: {}. Required: {}, Available: {}",
                        item.productId(), item.quantity(), product.getStockAvailable());
                createOutboxEvent(payload, metadata, incomingEvent.getEventId(), EVENT_TYPE_STOCK_RESERVE_FAILED);
                return;
            }
        }

        log.info("[Saga] Stock check passed for OrderId: {}. Reserving stock...", payload.orderId());

        for (OrderItemDto item : payload.items()) {
            Product product = productMap.get(item.productId());

            product.setStockAvailable(product.getStockAvailable() - item.quantity());
            product.setStockReserved(product.getStockReserved() + item.quantity());
        }

        productRepository.saveAll(productMap.values());

        createOutboxEvent(payload, metadata, incomingEvent.getEventId(), EVENT_TYPE_STOCK_RESERVED);
    }

    private void createOutboxEvent(SagaEventPayload payload, EventMetadata incomingMetadata,
            String causationEventId, String newEventType) {

        EventMetadata outgoingMetadata = EventMetadata.builder()
                .traceId(incomingMetadata.traceId())
                .causationId(causationEventId)
                .userId(incomingMetadata.userId())
                .timestamp(Instant.now().toEpochMilli())
                .build();

        OutboxEvent outgoingEvent = OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType(TOPIC_STOCKS)
                .aggregateId(payload.orderId().toString())
                .eventType(newEventType)
                .payload(jsonUtil.toJson(payload))
                .metadata(jsonUtil.toJson(outgoingMetadata))
                .status(STATUS_PENDING)
                .build();

        outboxEventRepository.save(outgoingEvent);

        log.info("[Saga] Created Outbox event: {} for OrderId: {}", newEventType, payload.orderId());
    }

    @Override
    @Transactional
    public void processPaymentResult(OutboxEvent incomingEvent) {
        SagaEventPayload payload = jsonUtil.fromJson(incomingEvent.getPayload(), SagaEventPayload.class);
        String eventType = incomingEvent.getEventType();

        List<Long> productIds = payload.items().stream()
                .map(OrderItemDto::productId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (EVENT_TYPE_PAYMENT_SUCCEEDED.equals(eventType)) {
            log.info("[Saga] Payment Succeeded for OrderId: {}. Confirming stock reservation...", payload.orderId());

            for (OrderItemDto item : payload.items()) {
                Product product = productMap.get(item.productId());
                if (product != null) {
                    int newReservedStock = product.getStockReserved() - item.quantity();

                    if (newReservedStock < 0) {
                        log.warn("[Saga] Stock inconsistency detected for ProductId: {}. Reserved stock is negative.",
                                product.getId());
                    }

                    product.setStockReserved(Math.max(0, newReservedStock));
                }
            }

        } else if (EVENT_TYPE_PAYMENT_FAILED.equals(eventType)) {
            log.warn("[Saga] Payment FAILED for OrderId: {}. Compensating (releasing) stock...", payload.orderId());

            for (OrderItemDto item : payload.items()) {
                Product product = productMap.get(item.productId());
                if (product != null) {
                    int newReservedStock = product.getStockReserved() - item.quantity();
                    int newAvailableStock = product.getStockAvailable() + item.quantity();

                    product.setStockReserved(Math.max(0, newReservedStock));
                    product.setStockAvailable(newAvailableStock);
                }
            }
        } else {
            log.warn("[Saga] Ignoring unknown event type: {}", eventType);
            return;
        }

        productRepository.saveAll(productMap.values());

        log.info("[Saga] Successfully processed event: {} for OrderId: {}.", eventType, payload.orderId());
    }
}
