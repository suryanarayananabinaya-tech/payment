package com.example.payment.strategy;

import com.example.payment.model.PaymentType;

public interface TypedPaymentStrategy extends PaymentStrategy {
    PaymentType getType();
}