package com.example.payment.service;

import com.example.payment.entity.OutboxEvent;
import com.example.payment.event.PaymentCreatedEvent;
import com.example.payment.messaging.PaymentEventProducer;
import com.example.payment.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxPublisherService outboxPublisherService;

    @Test
    void publishPendingEvents_success() throws Exception {
        OutboxEvent outbox = new OutboxEvent();
        outbox.setId(1L);
        outbox.setTopic("payment.created");
        outbox.setPayload("{\"eventId\":\"evt-1\"}");
        outbox.setStatus("NEW");

        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .eventId("evt-1")
                .transactionId("TXN-1")
                .userName("abinaya")
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("NEW"))
                .thenReturn(List.of(outbox));
        when(objectMapper.readValue(outbox.getPayload(), PaymentCreatedEvent.class))
                .thenReturn(event);

        outboxPublisherService.publishPendingEvents();

        verify(paymentEventProducer).publishPaymentCreated(event);
        verify(outboxEventRepository).save(outbox);

        assertEquals("PUBLISHED", outbox.getStatus());
        assertNotNull(outbox.getPublishedAt());
    }

    @Test
    void publishPendingEvents_marksFailed_whenPublishThrows() throws Exception {
        OutboxEvent outbox = new OutboxEvent();
        outbox.setId(1L);
        outbox.setTopic("payment.created");
        outbox.setPayload("{\"eventId\":\"evt-1\"}");
        outbox.setStatus("NEW");

        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .eventId("evt-1")
                .transactionId("TXN-1")
                .userName("abinaya")
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("NEW"))
                .thenReturn(List.of(outbox));
        when(objectMapper.readValue(outbox.getPayload(), PaymentCreatedEvent.class))
                .thenReturn(event);

        doThrow(new RuntimeException("kafka down"))
                .when(paymentEventProducer).publishPaymentCreated(event);

        outboxPublisherService.publishPendingEvents();

        verify(paymentEventProducer).publishPaymentCreated(event);
        verify(outboxEventRepository).save(outbox);

        assertEquals("FAILED", outbox.getStatus());
    }

    @Test
    void publishPendingEvents_marksPublished_forUnknownTopic() throws JsonProcessingException {
        OutboxEvent outbox = new OutboxEvent();
        outbox.setId(2L);
        outbox.setTopic("other.topic");
        outbox.setPayload("{}");
        outbox.setStatus("NEW");

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("NEW"))
                .thenReturn(List.of(outbox));

        outboxPublisherService.publishPendingEvents();

        verify(objectMapper, never()).readValue(anyString(), eq(PaymentCreatedEvent.class));
        verify(paymentEventProducer, never()).publishPaymentCreated(any());
        verify(outboxEventRepository).save(outbox);

        assertEquals("PUBLISHED", outbox.getStatus());
        assertNotNull(outbox.getPublishedAt());
    }

    @Test
    void publishPendingEvents_marksFailed_whenReadValueThrows() throws Exception {
        OutboxEvent outbox = new OutboxEvent();
        outbox.setId(3L);
        outbox.setTopic("payment.created");
        outbox.setPayload("bad-json");
        outbox.setStatus("NEW");

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("NEW"))
                .thenReturn(List.of(outbox));
        when(objectMapper.readValue("bad-json", PaymentCreatedEvent.class))
                .thenThrow(new RuntimeException("invalid json"));

        outboxPublisherService.publishPendingEvents();

        verify(paymentEventProducer, never()).publishPaymentCreated(any());
        verify(outboxEventRepository).save(outbox);

        assertEquals("FAILED", outbox.getStatus());
    }

    @Test
    void publishPendingEvents_handlesEmptyList() {
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("NEW"))
                .thenReturn(List.of());

        outboxPublisherService.publishPendingEvents();

        verify(outboxEventRepository, never()).save(any());
        verifyNoInteractions(paymentEventProducer);
        verifyNoInteractions(objectMapper);
    }
}