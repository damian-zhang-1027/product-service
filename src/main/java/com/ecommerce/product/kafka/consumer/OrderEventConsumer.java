package com.ecommerce.product.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.ecommerce.product.kafka.dto.BaseEventDto;
import com.ecommerce.product.kafka.dto.OrderCreatedEvent;
import com.ecommerce.product.service.stock.StockService;
import com.ecommerce.product.util.JsonUtil;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final StockService stockService;
    private final JsonUtil jsonUtil;
    private final Tracer tracer;

    @KafkaListener(topics = "orders")
    public void handleOrderEvent(String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("Received Kafka message from topic={}: key={}", topic, key);

        try {
            BaseEventDto baseEvent = jsonUtil.fromJson(message, BaseEventDto.class);

            if (baseEvent.eventType() == null) {
                log.warn("Received message with missing eventType: {}", message);
                return;
            }

            switch (baseEvent.eventType()) {
                case "ORDER_CREATED":
                    processOrderCreated(message);
                    break;
                default:
                    log.warn("Received unknown event type {}: {}", baseEvent.eventType(), message);
                    break;
            }

        } catch (Exception e) {
            // DLQ logic would go here
            log.error("Failed to process Kafka message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    private void processOrderCreated(String message) {
        OrderCreatedEvent event = jsonUtil.fromJson(message, OrderCreatedEvent.class);

        Span newSpan;
        if (event.metadata() != null && event.metadata().traceId() != null) {
            TraceContext parentContext = this.tracer.traceContextBuilder()
                    .traceId(event.metadata().traceId())
                    .spanId(event.metadata().causationId())
                    .build();

            newSpan = this.tracer.spanBuilder()
                    .setParent(parentContext)
                    .start();
        } else {
            newSpan = this.tracer.nextSpan().start();
        }

        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.name("consume-order-created").start())) {
            stockService.processOrderCreated(event);
        }
    }
}
