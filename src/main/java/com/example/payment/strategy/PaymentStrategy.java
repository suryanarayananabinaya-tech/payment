package com.example.payment.strategy;

import com.example.payment.model.PaymentRequest;

public interface PaymentStrategy {

    void pay (PaymentRequest paymentRequest);
}
