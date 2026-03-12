package com.example.payment.strategy;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentType;
import com.example.payment.repository.PaymentRepository;

public interface TypedPaymentStrategy extends PaymentStrategy {
    PaymentType getType();
}