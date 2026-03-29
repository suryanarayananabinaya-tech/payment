package com.example.payment.strategy;

import com.example.payment.model.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetBankingPaymentTest {

    private final NetBankingPayment strategy = new NetBankingPayment();

    @Test
    void getType() {
        assertEquals(PaymentType.NET_BANKING, strategy.getType());
    }
}