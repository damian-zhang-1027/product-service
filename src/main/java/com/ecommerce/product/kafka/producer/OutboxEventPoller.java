package com.ecommerce.product.kafka.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.product.model.db.entity.OutboxEvent;
import com.ecommerce.product.repository.db.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPoller {

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Pageable BATCH_PAGEABLE = PageRequest.of(0, 100);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SENT = "SENT";

    @Scheduled(fixedDelayString = "${kafka.poller.delay.ms:1000}")
    public void pollOutboxEvents() {

        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByUpdatedAtAsc(
                STATUS_PENDING,
                BATCH_PAGEABLE);

        if (events.isEmpty()) {
            return;
        }

        log.info("[Outbox] Found {} pending events to publish...", events.size());

        List<CompletableFuture<SendResult<String, String>>> futures = new ArrayList<>();
        List<OutboxEvent> sentEvents = new ArrayList<>();

        for (OutboxEvent event : events) {
            try {
                String topic = event.getAggregateType();
                String key = event.getAggregateId();
                String payload = event.getPayload();

                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);

                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(record);
                futures.add(future);

                event.setStatus(STATUS_SENT);
                sentEvents.add(event);

            } catch (Exception e) {
                // will retry in the next poller
                log.error("[Outbox] Failed to send eventId: {}. Error: {}", event.getEventId(), e.getMessage(), e);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        outboxEventRepository.saveAll(sentEvents);
        log.info("[Outbox] Successfully published {} events.", sentEvents.size());
    }
}
