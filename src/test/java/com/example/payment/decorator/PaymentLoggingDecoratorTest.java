package com.example.payment.decorator;

import com.example.payment.model.PaymentRequest;
import com.example.payment.strategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentLoggingDecoratorTest {

    @Mock
    private PaymentStrategy paymentStrategy;

    private PaymentLogging paymentLogging;

    @BeforeEach
    void setUp() {
        paymentLogging = new PaymentLogging(paymentStrategy);
    }

    @Test
    void pay_success_callsDelegate() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setEmail("test@example.com");
        paymentRequest.setAmount(BigDecimal.valueOf(100.00));
        paymentRequest.setCurrency("USD");

        doNothing().when(paymentStrategy).pay(paymentRequest);

        paymentLogging.pay(paymentRequest);

        verify(paymentStrategy, times(1)).pay(paymentRequest);
    }

    @Test
    void pay_success_noException() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setEmail("abinaya@example.com");
        paymentRequest.setAmount(BigDecimal.valueOf(250.00));
        paymentRequest.setCurrency("INR");

        doNothing().when(paymentStrategy).pay(paymentRequest);

        paymentLogging.pay(paymentRequest);

        verify(paymentStrategy).pay(paymentRequest);
        verifyNoMoreInteractions(paymentStrategy);
    }

    @Test
    void pay_failure_throwsException() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setEmail("fail@example.com");
        paymentRequest.setAmount(BigDecimal.valueOf(500.00));
        paymentRequest.setCurrency("EUR");

        RuntimeException runtimeException = new RuntimeException("Payment gateway failure");

        doThrow(runtimeException).when(paymentStrategy).pay(paymentRequest);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> paymentLogging.pay(paymentRequest)
        );

        verify(paymentStrategy, times(1)).pay(paymentRequest);
        org.junit.jupiter.api.Assertions.assertEquals("Payment gateway failure", thrown.getMessage());
    }
}