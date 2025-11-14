package com.ecommerce.product.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.ecommerce.product.model.db.entity.OutboxEvent;
import com.ecommerce.product.service.stock.StockSagaService;
import com.ecommerce.product.util.JsonUtil;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final StockSagaService stockSagaService;
    private final JsonUtil jsonUtil;
    private final Tracer tracer;

    private static final String EVENT_TYPE_PAYMENT_SUCCEEDED = "PAYMENT_SUCCEEDED";
    private static final String EVENT_TYPE_PAYMENT_FAILED = "PAYMENT_FAILED";

    @KafkaListener(topics = "payments", groupId = "product-service-group")
    public void handlePaymentEvent(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        Span consumerSpan = tracer.spanBuilder("consume-payment-result").startSpan();

        try {
            OutboxEvent incomingEvent = jsonUtil.fromJson(message, OutboxEvent.class);
            String eventType = incomingEvent.getEventType();

            if (EVENT_TYPE_PAYMENT_SUCCEEDED.equals(eventType) || EVENT_TYPE_PAYMENT_FAILED.equals(eventType)) {

                log.info("[Consumer] Received event: {}. Key: {}", eventType, key);

                consumerSpan.setAttribute("kafka.event.type", eventType);
                consumerSpan.setAttribute("kafka.event.id", incomingEvent.getEventId());
                consumerSpan.setAttribute("kafka.aggregate.id", incomingEvent.getAggregateId());

                try (Scope ws = consumerSpan.makeCurrent()) {
                    stockSagaService.processPaymentResult(incomingEvent);
                }
            }

        } catch (Exception e) {
            log.error("[Consumer] Failed to process event from 'payments' topic. Key: {}. Error: {}",
                    key, e.getMessage(), e);
            consumerSpan.setStatus(StatusCode.ERROR, e.getMessage());
            consumerSpan.recordException(e);
        }
    }
}
