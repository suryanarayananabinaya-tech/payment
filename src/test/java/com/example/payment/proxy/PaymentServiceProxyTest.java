package com.example.payment.proxy;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentType;
import com.example.payment.strategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentProxyTest {

    private PaymentStrategy strategy;
    private PaymentProxy proxy;

    @BeforeEach
    void setup() {
        strategy = mock(PaymentStrategy.class);
        proxy = new PaymentProxy(strategy);
    }

    private PaymentRequest validRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setEmail("test@test.com");
        request.setPaymentType(PaymentType.CARD);
        return request;
    }

    // ✅ Happy path
    @Test
    void pay_success() {
        PaymentRequest request = validRequest();

        proxy.pay(request);

        verify(strategy).pay(request);
    }

    // ❌ amount null
    @Test
    void pay_amountNull() {
        PaymentRequest request = validRequest();
        request.setAmount(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> proxy.pay(request)
        );

        assertEquals("Amount must be greater than zero", ex.getMessage());
        verify(strategy, never()).pay(any());
    }

    // ❌ amount zero/negative
    @Test
    void pay_amountZero() {
        PaymentRequest request = validRequest();
        request.setAmount(BigDecimal.ZERO);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> proxy.pay(request)
        );

        assertEquals("Amount must be greater than zero", ex.getMessage());
        verify(strategy, never()).pay(any());
    }

    // ❌ email empty
    @Test
    void pay_emailEmpty() {
        PaymentRequest request = validRequest();
        request.setEmail(" ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> proxy.pay(request)
        );

        assertEquals("Email is mandatory", ex.getMessage());
        verify(strategy, never()).pay(any());
    }

    // ❌ payment type null
    @Test
    void pay_paymentTypeNull() {
        PaymentRequest request = validRequest();
        request.setPaymentType(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> proxy.pay(request)
        );

        assertEquals("PaymentType is mandatory", ex.getMessage());
        verify(strategy, never()).pay(any());
    }
}