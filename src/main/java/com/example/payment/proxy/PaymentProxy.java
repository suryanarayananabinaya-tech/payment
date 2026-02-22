package com.example.payment.proxy;

import com.example.payment.model.PaymentRequest;
import com.example.payment.strategy.PaymentStrategy;

import java.math.BigDecimal;

public class PaymentProxy implements PaymentStrategy {

    private final PaymentStrategy paymentStrategy;

    public PaymentProxy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    @Override
    public void pay(PaymentRequest paymentRequest) {

        BigDecimal amount = paymentRequest.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (paymentRequest.getEmail() == null || paymentRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is mandatory");
        }

        if (paymentRequest.getPaymentType() == null) {
            throw new IllegalArgumentException("PaymentType is mandatory");
        }

        paymentStrategy.pay(paymentRequest);
    }
}