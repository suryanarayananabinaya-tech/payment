package com.example.payment.service;

import com.example.payment.entity.OutboxEvent;
import com.example.payment.event.PaymentCreatedEvent;
import com.example.payment.messaging.PaymentEventProducer;
import com.example.payment.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("NEW");

        for (OutboxEvent outbox : events) {
            try {
                if ("payment.created".equals(outbox.getTopic())) {
                    PaymentCreatedEvent event =
                            objectMapper.readValue(outbox.getPayload(), PaymentCreatedEvent.class);

                    paymentEventProducer.publishPaymentCreated(event);
                }
                outbox.setStatus("PUBLISHED");
                outbox.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(outbox);

            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}", outbox.getId(), e);
                outbox.setStatus("FAILED");
                outboxEventRepository.save(outbox);
            }
        }
    }
}