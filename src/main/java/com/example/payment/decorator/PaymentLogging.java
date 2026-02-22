package com.example.payment.decorator;

import com.example.payment.model.PaymentRequest;
import com.example.payment.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record PaymentLogging(PaymentStrategy delegate) implements PaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PaymentLogging.class);

    @Override
    public void pay(PaymentRequest paymentRequest) {
        logger.info("Payment request started for user: {} amount: {} {}",
                paymentRequest.getEmail(), paymentRequest.getAmount(), paymentRequest.getCurrency());

        long start = System.currentTimeMillis();
        try {
            delegate.pay(paymentRequest);
            logger.info("Payment request finished successfully");
        } finally {
            long duration = System.currentTimeMillis() - start;
            logger.info("Payment request duration: {} ms", duration);
        }
    }
}