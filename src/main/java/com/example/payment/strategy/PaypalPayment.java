package com.example.payment.strategy;

import com.example.payment.model.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaypalPayment implements PaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PaypalPayment.class);


    @Override
    public void pay(PaymentRequest paymentRequest) {

        logger.info("Processing payment of {} {} using PAYPAL for email {}",
                paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getEmail());
        // Here you can integrate with PayPal API/SDK
        logger.info("PayPal payment completed successfully");
    }
}
