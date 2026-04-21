package com.example.payment.messaging;

import com.example.payment.entity.ProcessedEvent;
import com.example.payment.event.PaymentCreatedEvent;
import com.example.payment.repository.ProcessedEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    private PaymentCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = new PaymentCreatedEvent();
        event.setTransactionId("TXN-123");
        event.setAmount(BigDecimal.valueOf(100));
    }

    @Test
    void consumeEvent() {
        ConsumerRecord<String, PaymentCreatedEvent> record =
                new ConsumerRecord<>("payment.created", 0, 0L, "key1", event);

        when(processedEventRepository.existsById("TXN-123")).thenReturn(false);

        paymentEventConsumer.consumePaymentCreatedEvent(record);

        verify(processedEventRepository).existsById("TXN-123");

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(captor.capture());
        assertEquals("TXN-123", captor.getValue().getTransactionId());
    }

    @Test
    void nullEvent() {
        ConsumerRecord<String, PaymentCreatedEvent> record =
                new ConsumerRecord<>("payment.created", 0, 0L, "key1", null);

        paymentEventConsumer.consumePaymentCreatedEvent(record);

        verify(processedEventRepository, never()).existsById(anyString());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void missingTransactionId() {
        event.setTransactionId(null);

        ConsumerRecord<String, PaymentCreatedEvent> record =
                new ConsumerRecord<>("payment.created", 0, 0L, "key1", event);

        paymentEventConsumer.consumePaymentCreatedEvent(record);

        verify(processedEventRepository, never()).existsById(anyString());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void duplicateEvent() {
        ConsumerRecord<String, PaymentCreatedEvent> record =
                new ConsumerRecord<>("payment.created", 0, 0L, "key1", event);

        when(processedEventRepository.existsById("TXN-123")).thenReturn(true);

        paymentEventConsumer.consumePaymentCreatedEvent(record);

        verify(processedEventRepository).existsById("TXN-123");
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void duplicateOnSave() {
        ConsumerRecord<String, PaymentCreatedEvent> record =
                new ConsumerRecord<>("payment.created", 0, 0L, "key1", event);

        when(processedEventRepository.existsById("TXN-123")).thenReturn(false);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(processedEventRepository).save(any(ProcessedEvent.class));

        paymentEventConsumer.consumePaymentCreatedEvent(record);

        verify(processedEventRepository).existsById("TXN-123");
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void exceptionDuringExistsCheck() {
        ConsumerRecord<String, PaymentCreatedEvent> record =
                new ConsumerRecord<>("payment.created", 0, 0L, "key1", event);

        when(processedEventRepository.existsById("TXN-123"))
                .thenThrow(new RuntimeException("db error"));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> paymentEventConsumer.consumePaymentCreatedEvent(record)
        );

    }

    @Test
    void consumeEventWithTraceHeader() {
        RecordHeaders headers = new RecordHeaders();
        headers.add("traceId", "trace-123".getBytes());

        ConsumerRecord<String, PaymentCreatedEvent> record =
                new ConsumerRecord<>("payment.created", 0, 0L, "key1", event);

        when(processedEventRepository.existsById("TXN-123")).thenReturn(false);

        paymentEventConsumer.consumePaymentCreatedEvent(record);

        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }
}