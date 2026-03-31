package com.example.payment.messaging;

import com.example.payment.entity.ProcessedEvent;
import com.example.payment.event.PaymentCreatedEvent;
import com.example.payment.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
            topics = "${payment.kafka.topic.created}",
            groupId = "payment-audit-group"
    )
    public void consumePaymentCreatedEvent(ConsumerRecord<String, PaymentCreatedEvent> record) {

        Header traceHeader = record.headers().lastHeader("traceId");
        String traceId = traceHeader != null
                ? new String(traceHeader.value(), StandardCharsets.UTF_8)
                : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        try {
            PaymentCreatedEvent event = record.value();
            if (event == null) {
                log.warn("Received null payment event. Skipping.");
                return;
            }

            String transactionId = event.getTransactionId();
            if (transactionId == null || transactionId.isBlank()) {
                log.error("Missing transactionId in payment event. Event={}", event);
                return;
            }

            log.info("Received payment event. TransactionId={}", transactionId);

            if (processedEventRepository.existsById(transactionId)) {
                log.info("Duplicate payment event detected. Skipping TransactionId={}", transactionId);
                return;
            }

            processAudit(event);

            try {
                processedEventRepository.save(new ProcessedEvent(transactionId));
            } catch (DataIntegrityViolationException ex) {
                log.info("Duplicate payment event detected during save. TransactionId={}", transactionId);
            }

            log.info("Successfully processed payment event. TransactionId={}", transactionId);
        } catch (Exception ex) {
            log.error("Error while consuming payment event. Record={}", record, ex);
            throw ex;
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