package com.ecommerce.product.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.ecommerce.product.kafka.dto.BaseEventDto;
import com.ecommerce.product.kafka.dto.EventMetadata;
import com.ecommerce.product.kafka.dto.PaymentEventDto;
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
public class PaymentEventConsumer {

    private final StockService stockService;
    private final JsonUtil jsonUtil;
    private final Tracer tracer;

    @KafkaListener(topics = "payments")
    public void handlePaymentEvent(String message,
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
                case "PAYMENT_SUCCEEDED":
                    processPaymentSucceeded(message);
                    break;
                case "PAYMENT_FAILED":
                    processPaymentFailed(message);
                    break;
                default:
                    log.warn("Received unknown event type {} on 'payments' topic: {}", baseEvent.eventType(), message);
                    break;
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    private void processPaymentSucceeded(String message) {
        PaymentEventDto event = jsonUtil.fromJson(message, PaymentEventDto.class);

        try (Tracer.SpanInScope ws = createSpanFromEvent(event.metadata(), "consume-payment-succeeded")) {
            stockService.processPaymentSucceeded(event);
        }
    }

    private void processPaymentFailed(String message) {
        PaymentEventDto event = jsonUtil.fromJson(message, PaymentEventDto.class);

        try (Tracer.SpanInScope ws = createSpanFromEvent(event.metadata(), "consume-payment-failed")) {
            stockService.processPaymentFailed(event);
        }
    }

    /**
     * from event metadata to new trace span
     */
    private Tracer.SpanInScope createSpanFromEvent(EventMetadata metadata, String spanName) {
        Span newSpan;

        if (metadata != null && metadata.traceId() != null) {
            TraceContext parentContext = this.tracer.traceContextBuilder()
                    .traceId(metadata.traceId())
                    .spanId(metadata.causationId())
                    .build();

            newSpan = this.tracer.spanBuilder()
                    .setParent(parentContext)
                    .name(spanName)
                    .start();
        } else {
            newSpan = this.tracer.nextSpan()
                    .name(spanName)
                    .start();
        }

        return this.tracer.withSpan(newSpan);
    }
}
