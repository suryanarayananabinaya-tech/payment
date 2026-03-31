package com.example.payment.service;

import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.dto.PaymentResponseDTO;
import com.example.payment.entity.OutboxEvent;
import com.example.payment.entity.Payment;
import com.example.payment.entity.User;
import com.example.payment.factory.PaymentFactory;
import com.example.payment.model.PaymentStatus;
import com.example.payment.model.PaymentType;
import com.example.payment.repository.OutboxEventRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.UserRepository;
import com.example.payment.strategy.TypedPaymentStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentFactory paymentFactory;

    @Mock
    private PaymentExecutionService paymentExecutionService;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TypedPaymentStrategy strategy;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequestDTO buildRequest() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency("USD");
        request.setEmail("test@test.com");
        request.setPaymentType(PaymentType.CARD);
        return request;
    }

    @Test
    void processPayment_success() throws Exception {
        PaymentRequestDTO request = buildRequest();

        User user = new User();
        user.setUsername("abinaya");

        when(paymentRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());

        when(paymentFactory.getStrategy(PaymentType.CARD))
                .thenReturn(strategy);

        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.of(user));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"event\":\"ok\"}");

        PaymentResponseDTO response =
                paymentService.processPayment("abinaya", "key123", request);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("Payment processed successfully", response.getMessage());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals(PaymentType.CARD, response.getPaymentType());
        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN-"));

        verify(paymentRepository).findByIdempotencyKey("key123");
        verify(paymentFactory).getStrategy(PaymentType.CARD);
        verify(paymentExecutionService).execute(any(), any());
        verify(userRepository).findByUsername("abinaya");
        verify(paymentRepository).save(any(Payment.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));
        verify(objectMapper).writeValueAsString(any());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(BigDecimal.valueOf(100), savedPayment.getAmount());
        assertEquals("USD", savedPayment.getCurrency());
        assertEquals(PaymentType.CARD, savedPayment.getPaymentType());
        assertEquals(PaymentStatus.SUCCESS, savedPayment.getStatus());
        assertEquals("key123", savedPayment.getIdempotencyKey());
        assertNotNull(savedPayment.getTransactionId());
    }

    @Test
    void processPayment_duplicate() throws Exception {
        Payment existing = new Payment();
        existing.setStatus(PaymentStatus.SUCCESS);
        existing.setAmount(BigDecimal.valueOf(100));
        existing.setCurrency("USD");
        existing.setPaymentType(PaymentType.CARD);
        existing.setTransactionId("txn-123");

        when(paymentRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.of(existing));

        PaymentResponseDTO response =
                paymentService.processPayment("abinaya", "key123", buildRequest());

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("Duplicate request - returning existing payment", response.getMessage());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals(PaymentType.CARD, response.getPaymentType());
        assertEquals("txn-123", response.getTransactionId());

        verify(paymentRepository).findByIdempotencyKey("key123");
        verify(paymentFactory, never()).getStrategy(any());
        verify(paymentExecutionService, never()).execute(any(), any());
        verify(userRepository, never()).findByUsername(anyString());
        verify(paymentRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void processPayment_invalidType() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPaymentType(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.processPayment("abinaya", "key123", request)
        );

        assertEquals("INVALID PAYMENT TYPE", ex.getMessage());

        verify(paymentFactory, never()).getStrategy(any());
        verify(paymentExecutionService, never()).execute(any(), any());
        verify(paymentRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void processPayment_userNotFound() throws Exception {
        PaymentRequestDTO request = buildRequest();

        when(paymentRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());

        when(paymentFactory.getStrategy(PaymentType.CARD))
                .thenReturn(strategy);

        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> paymentService.processPayment("abinaya", "key123", request)
        );

        assertEquals("Invalid username or password", ex.getMessage());

        verify(paymentExecutionService).execute(any(), any());
        verify(userRepository).findByUsername("abinaya");
        verify(paymentRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void processPayment_jsonError() throws Exception {
        PaymentRequestDTO request = buildRequest();

        User user = new User();
        user.setUsername("abinaya");

        when(paymentRepository.findByIdempotencyKey("key123"))
                .thenReturn(Optional.empty());

        when(paymentFactory.getStrategy(PaymentType.CARD))
                .thenReturn(strategy);

        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.of(user));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("json error") {});

        assertThrows(
                JsonProcessingException.class,
                () -> paymentService.processPayment("abinaya", "key123", request)
        );

        verify(paymentExecutionService).execute(any(), any());
        verify(paymentRepository).save(any(Payment.class));
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void processPayment_blankIdempotencyKey() throws Exception {
        PaymentRequestDTO request = buildRequest();

        User user = new User();
        user.setUsername("abinaya");

        when(paymentFactory.getStrategy(PaymentType.CARD))
                .thenReturn(strategy);

        when(userRepository.findByUsername("abinaya"))
                .thenReturn(Optional.of(user));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"event\":\"ok\"}");

        PaymentResponseDTO response =
                paymentService.processPayment("abinaya", "   ", request);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());

        verify(paymentRepository, never()).findByIdempotencyKey(anyString());
        verify(paymentExecutionService).execute(any(), any());
        verify(paymentRepository).save(any(Payment.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }
}