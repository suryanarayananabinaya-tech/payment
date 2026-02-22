package com.example.payment.strategy;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentType;

public interface PaymentStrategy {

    void pay (PaymentRequest paymentRequest);
}
