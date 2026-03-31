package com.example.payment.strategy;

import com.example.payment.model.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreditCardPaymentTest {

    private final CreditCardPayment strategy = new CreditCardPayment();

    @Test
    void getType() {
        assertEquals(PaymentType.CARD, strategy.getType());
    }
}