package com.example.payment.messaging;

import com.example.payment.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    public void publishPaymentCreated(PaymentCreatedEvent event) {
        String traceId = MDC.get("traceId");

        ProducerRecord<String, PaymentCreatedEvent> record =
                new ProducerRecord<>("payment.created", event.getTransactionId(), event);

        if (traceId != null) {
            record.headers().add("traceId", traceId.getBytes(StandardCharsets.UTF_8));
        }

        kafkaTemplate.send(record);
    }
}