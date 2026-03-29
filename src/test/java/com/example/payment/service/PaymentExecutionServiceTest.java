package com.example.payment.service;

import com.example.payment.exception.ExternalServiceException;
import com.example.payment.model.PaymentRequest;
import com.example.payment.template.PaymentProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentExecutionServiceTest {

    @Mock
    private PaymentProcessor processor;

    @Mock
    private PaymentRequest request;

    @InjectMocks
    private PaymentExecutionService paymentExecutionService;

    @Test
    void execute_callsProcessor() {
        paymentExecutionService.execute(processor, request);

        verify(processor).processPayment(request);
    }

    @Test
    void fallbackPayment_throwsExternalServiceException() {
        RuntimeException cause = new RuntimeException("gateway down");

        ExternalServiceException ex = assertThrows(
                ExternalServiceException.class,
                () -> paymentExecutionService.fallbackPayment(processor, request, cause)
        );

        assertEquals("External payment service unavailable. Try later.", ex.getMessage());
    }
}