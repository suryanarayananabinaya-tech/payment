package com.example.payment.strategy;

import com.example.payment.model.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditCardPayment implements PaymentStrategy{

    private static final Logger logger = LoggerFactory.getLogger(CreditCardPayment.class);


    @Override
    public void pay(PaymentRequest paymentRequest) {

        logger.info("Processing payment of {} {} using Credit Card for email {}",
                paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getEmail());
        // Here we can integrate with PayPal API/SDK
        logger.info("Credit card payment completed successfully");

    }
}
