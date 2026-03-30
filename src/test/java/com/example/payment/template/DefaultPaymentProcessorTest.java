package com.example.payment.template;

import com.example.payment.model.PaymentRequest;
import com.example.payment.strategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultPaymentProcessorTest {

    private PaymentStrategy paymentStrategy;
    private DefaultPaymentProcessor processor;

    @BeforeEach
    void setUp() {
        paymentStrategy = mock(PaymentStrategy.class);
        processor = new DefaultPaymentProcessor(paymentStrategy);
    }

    @Test
    void executePayment_delegatesToStrategy() {
        PaymentRequest request = new PaymentRequest();

        processor.executePayment(request);

        verify(paymentStrategy).pay(request);
    }

    @Test
    void processPayment_runsFullTemplateAndDelegatesToStrategy() {
        PaymentRequest request = new PaymentRequest();

        processor.processPayment(request);

        verify(paymentStrategy).pay(request);
    }
}