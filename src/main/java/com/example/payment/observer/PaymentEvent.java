package com.example.payment.observer;

import com.example.payment.model.PaymentRequest;
import lombok.Getter;

@Getter
public class PaymentEvent {
    private final PaymentRequest paymentRequest;

    public PaymentEvent(PaymentRequest paymentRequest)
    {
        this.paymentRequest = paymentRequest;
    }

}
