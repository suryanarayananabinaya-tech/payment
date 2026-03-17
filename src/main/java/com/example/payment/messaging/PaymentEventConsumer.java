package com.example.payment.messaging;

import com.example.payment.event.PaymentCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Slf4j
public class PaymentEventConsumer {

    @KafkaListener(
            topics = "payment.created",
            groupId = "payment-audit-group"
    )
    public void consumePaymentCreatedEvent(ConsumerRecord<String, PaymentCreatedEvent> record, Acknowledgment acknowledgment) {

        // Extract traceId from Kafka header
        Header traceHeader = record.headers().lastHeader("traceId");

        String traceId = traceHeader != null
                ? new String(traceHeader.value(), StandardCharsets.UTF_8)
                : UUID.randomUUID().toString();

        MDC.put("traceId", traceId);

        try {
            PaymentCreatedEvent event = record.value();
            log.info("Received payment event. TransactionId={}", event.getTransactionId());
            processAudit(event);
            acknowledgment.acknowledge();
            log.info("Acknowledged payment event. TransactionId={}", event.getTransactionId());
        } finally {
            MDC.clear();
        }
    }

    private void processAudit(PaymentCreatedEvent event) {
        log.info("Audit log for payment {} amount {}",
                event.getTransactionId(),
                event.getAmount());

    }
}