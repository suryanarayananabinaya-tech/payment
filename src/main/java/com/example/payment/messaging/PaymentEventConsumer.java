package com.example.payment.messaging;

import com.example.payment.entity.ProcessedEvent;
import com.example.payment.event.PaymentCreatedEvent;
import com.example.payment.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Slf4j
public class PaymentEventConsumer {

    private final ProcessedEventRepository processedEventRepository;

    public PaymentEventConsumer(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }


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
            if (event == null) {
                log.warn("Received null payment event. Skipping.");
                acknowledgment.acknowledge();
                return;
            }
            String transactionId = event.getTransactionId();
            if (transactionId == null || transactionId.isBlank()) {
                log.error("Missing transactionId in payment event. Event={}", event);
                acknowledgment.acknowledge();
                return;
            }

            log.info("Received payment event. TransactionId={}", event.getTransactionId());
            // First defensive duplicate check
            if (processedEventRepository.existsById(transactionId)) {
                log.info("Duplicate payment event detected. Skipping TransactionId={}", transactionId);
                acknowledgment.acknowledge();
                return;
            }
            processAudit(event);
            // Save processed marker after successful processing
            // Unique constraint on transactionId/idempotency key is strongly recommended
            try {
                processedEventRepository.save(new ProcessedEvent(transactionId));
            } catch (DataIntegrityViolationException ex) {
                log.info("Duplicate payment event detected during save. TransactionId={}", transactionId);
            }

            acknowledgment.acknowledge();
            log.info("Acknowledged payment event. TransactionId={}", event.getTransactionId());
        } catch (Exception ex) {
            log.error("Error while consuming payment event. Record={}", record, ex);
            throw ex; // let Kafka retry / error handler manage it
        }
        finally {
            MDC.clear();
        }
    }

    private void processAudit(PaymentCreatedEvent event) {
        log.info("Audit log for payment {} amount {}",
                event.getTransactionId(),
                event.getAmount());

    }
}