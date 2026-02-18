package com.example.payment.proxy;

import com.example.payment.model.PaymentRequest;
import com.example.payment.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentProxy  implements PaymentStrategy {

    private static final Logger logger  = LoggerFactory.getLogger(PaymentProxy.class);

    private final  PaymentStrategy paymentStrategy;

    public PaymentProxy(PaymentStrategy PaymentStrategy) {
        this.paymentStrategy = PaymentStrategy;
    }

    @Override
    public void pay(PaymentRequest paymentRequest) {
        if( paymentRequest.getAmount() <= 0 ){
            logger.error("Payment failed: Amount must be greater than zero");
            throw new RuntimeException("Amount must be greated than zero");
        }
        if (paymentRequest.getEmail() == null || paymentRequest.getEmail().isEmpty()) {
            logger.error("Payment failed: Email is mandatory");
            throw new RuntimeException("Email is mandatory");
        }
        logger.info("Payment request passed validation checks");
        paymentStrategy.pay(paymentRequest);


    }
}
