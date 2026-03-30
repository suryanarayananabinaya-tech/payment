package com.example.payment.messaging;

import com.example.payment.event.PaymentCreatedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventProducerTest {

    @Mock
    private KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    @InjectMocks
    private PaymentEventProducer paymentEventProducer;

    private PaymentCreatedEvent event;

    @BeforeEach
    void setup() {
        event = new PaymentCreatedEvent();
        event.setTransactionId("TXN-123");
        event.setAmount(BigDecimal.valueOf(100));

        ReflectionTestUtils.setField(paymentEventProducer, "paymentCreatedTopic", "payment.created");
        MDC.clear();
    }

    @Test
    void publishEvent() {
        CompletableFuture<SendResult<String, PaymentCreatedEvent>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        paymentEventProducer.publishPaymentCreated(event);

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    void publishEventWithTraceId() {
        MDC.put("traceId", "trace-123");

        CompletableFuture<SendResult<String, PaymentCreatedEvent>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        ArgumentCaptor<ProducerRecord<String, PaymentCreatedEvent>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);

        paymentEventProducer.publishPaymentCreated(event);

        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, PaymentCreatedEvent> record = captor.getValue();

        assertEquals("payment.created", record.topic());
        assertEquals("TXN-123", record.key());
        assertEquals(event, record.value());
        assertNotNull(record.headers().lastHeader("traceId"));
        assertEquals(
                "trace-123",
                new String(record.headers().lastHeader("traceId").value(), StandardCharsets.UTF_8)
        );
    }

    @Test
    void publishEventWithoutTraceId() {
        CompletableFuture<SendResult<String, PaymentCreatedEvent>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        ArgumentCaptor<ProducerRecord<String, PaymentCreatedEvent>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);

        paymentEventProducer.publishPaymentCreated(event);

        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, PaymentCreatedEvent> record = captor.getValue();

        assertNull(record.headers().lastHeader("traceId"));
    }

    @Test
    void nullEvent() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentEventProducer.publishPaymentCreated(null));

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void missingTransactionId() {
        event.setTransactionId(null);

        assertThrows(IllegalArgumentException.class,
                () -> paymentEventProducer.publishPaymentCreated(event));

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void sendFailure() {
        CompletableFuture<SendResult<String, PaymentCreatedEvent>> future =
                CompletableFuture.failedFuture(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        paymentEventProducer.publishPaymentCreated(event);

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }
}