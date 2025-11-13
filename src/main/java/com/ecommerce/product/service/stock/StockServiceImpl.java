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
import com.ecommerce.product.kafka.dto.OrderCreatedEvent;
import com.ecommerce.product.kafka.dto.OrderItemDto;
import com.ecommerce.product.kafka.dto.PaymentEventDto;
import com.ecommerce.product.kafka.dto.StockReserveFailedEvent;
import com.ecommerce.product.kafka.dto.StockReservedEvent;
import com.ecommerce.product.model.db.entity.OutboxEvent;
import com.ecommerce.product.model.db.entity.Product;
import com.ecommerce.product.repository.db.OutboxEventRepository;
import com.ecommerce.product.repository.db.ProductRepository;
import com.ecommerce.product.util.JsonUtil;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final JsonUtil jsonUtil;
    private final Tracer tracer;

    @Override
    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {

        Long orderId = event.orderId();
        List<OrderItemDto> items = event.items();
        Long totalAmount = event.totalAmount();

        log.info("Processing ORDER_CREATED event for orderId: {}", orderId);

        List<Long> productIds = items.stream()
                .map(OrderItemDto::productId)
                .toList();

        List<Product> products = productRepository.findAllByIdInWithPessimisticWrite(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        String failureReason = null;
        for (OrderItemDto item : items) {
            Product product = productMap.get(item.productId());
            if (product == null) {
                failureReason = "Product not found: " + item.productId();
                break;
            }
            if (product.getStockAvailable() < item.quantity()) {
                failureReason = "Insufficient stock for product: " + item.productId();
                break;
            }
        }

        if (failureReason == null) {
            log.info("Stock available. Reserving stock for orderId: {}", orderId);

            for (OrderItemDto item : items) {
                Product product = productMap.get(item.productId());
                product.setStockAvailable(product.getStockAvailable() - item.quantity());
                product.setStockReserved(product.getStockReserved() + item.quantity());
            }
            productRepository.saveAll(products);

            StockReservedEvent payload = new StockReservedEvent(
                    orderId,
                    totalAmount,
                    items);
            createOutboxEvent(
                    "stock",
                    orderId.toString(),
                    "STOCK_RESERVED",
                    jsonUtil.toJson(payload),
                    event.metadata());

        } else {
            log.warn("Stock reservation failed for orderId: {}. Reason: {}", orderId, failureReason);

            StockReserveFailedEvent payload = new StockReserveFailedEvent(orderId, failureReason);
            createOutboxEvent(
                    "stock",
                    orderId.toString(),
                    "STOCK_RESERVE_FAILED",
                    jsonUtil.toJson(payload),
                    event.metadata());
        }
    }

    private void createOutboxEvent(String aggregateType, String aggregateId, String eventType, String payload,
            EventMetadata metadata) {

        String newTraceId = "N/A_TRACE_ID";
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            newTraceId = currentSpan.context().traceId();
        }
        EventMetadata newMetadata = new EventMetadata(
                newTraceId,
                metadata.traceId(),
                metadata.userId(),
                Instant.now().toEpochMilli());

        OutboxEvent event = OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .metadata(jsonUtil.toJson(newMetadata))
                .status("PENDING")
                .build();

        outboxEventRepository.save(event);
    }

    @Override
    @Transactional
    public void processPaymentSucceeded(PaymentEventDto event) {
        Long orderId = event.orderId();
        log.info("Processing PAYMENT_SUCCEEDED for orderId: {}", orderId);

        List<Long> productIds = event.items().stream().map(OrderItemDto::productId).toList();
        List<Product> products = productRepository.findAllByIdInWithPessimisticWrite(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (OrderItemDto item : event.items()) {
            Product product = productMap.get(item.productId());
            if (product == null) {
                log.error("CRITICAL: Product not found during PAYMENT_SUCCEEDED compensation. ProductId: {}",
                        item.productId());
                continue;
            }
            if (product.getStockReserved() < item.quantity()) {
                log.error(
                        "CRITICAL: StockReserved mismatch during PAYMENT_SUCCEEDED. OrderId: {}, ProductId: {}, Expected: {}, Found: {}",
                        orderId, item.productId(), item.quantity(), product.getStockReserved());
            }

            product.setStockReserved(Math.max(0, product.getStockReserved() - item.quantity()));
        }

        productRepository.saveAll(products);
        log.info("Stock consumption successful for orderId: {}", orderId);
    }

    @Override
    @Transactional
    public void processPaymentFailed(PaymentEventDto event) {
        Long orderId = event.orderId();
        log.info("Processing PAYMENT_FAILED (Compensation) for orderId: {}", orderId);

        List<Long> productIds = event.items().stream().map(OrderItemDto::productId).toList();
        List<Product> products = productRepository.findAllByIdInWithPessimisticWrite(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (OrderItemDto item : event.items()) {
            Product product = productMap.get(item.productId());
            if (product == null) {
                log.error("CRITICAL: Product not found during PAYMENT_FAILED compensation. ProductId: {}",
                        item.productId());
                continue;
            }
            if (product.getStockReserved() < item.quantity()) {
                log.error("CRITICAL: StockReserved mismatch during PAYMENT_FAILED. OrderId: {}, ProductId: {}",
                        orderId, item.productId());
            }

            int quantityToReturn = Math.min(item.quantity(), product.getStockReserved());

            product.setStockReserved(product.getStockReserved() - quantityToReturn);
            product.setStockAvailable(product.getStockAvailable() + quantityToReturn);
        }

        productRepository.saveAll(products);
        log.info("Stock compensation successful for orderId: {}", orderId);
    }
}
