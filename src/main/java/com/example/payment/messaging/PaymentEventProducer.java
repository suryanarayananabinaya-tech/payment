package com.example.payment.messaging;

import com.example.payment.event.PaymentCreatedEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    @PostConstruct
    public void init() {
        log.info("BOOTSTRAP AT STARTUP: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
    }

    private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    private final Environment environment;

    @Value("${payment.kafka.topic.created}")
    private String paymentCreatedTopic;

    public void publishPaymentCreated(PaymentCreatedEvent event) {
        log.info("BOOTSTRAP: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
        if (event == null || event.getTransactionId() == null) {
            throw new IllegalArgumentException("Invalid payment event");
        }

        String traceId = MDC.get("traceId");
        log.info("Kafka topic configured: {}", paymentCreatedTopic);
        ProducerRecord<String, PaymentCreatedEvent> record =
                new ProducerRecord<>(paymentCreatedTopic, event.getTransactionId(), event);

        if (traceId != null) {
            record.headers().add("traceId", traceId.getBytes(StandardCharsets.UTF_8));
        }

        log.info("Publishing payment event. TransactionId={}", event.getTransactionId());

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send payment event {}", event.getTransactionId(), ex);
            } else {
                log.info("Message sent successfully. Offset={}, Partition={}",
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}