package com.example.payment.template;

import com.example.payment.model.PaymentRequest;
import com.example.payment.strategy.PaymentStrategy;

public class DefaultPaymentProcessor extends PaymentProcessor {

    private final PaymentStrategy paymentStrategy;

    public DefaultPaymentProcessor(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }


    @Override
    protected void executePayment(PaymentRequest request) {
        paymentStrategy.pay(request);



    }
}
