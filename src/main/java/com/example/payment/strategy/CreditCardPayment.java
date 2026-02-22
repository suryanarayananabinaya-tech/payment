package com.example.payment.strategy;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreditCardPayment implements TypedPaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardPayment.class);


    @Override
    public PaymentType getType() {
        return PaymentType.CARD;
    }

    @Override
    public void pay(PaymentRequest paymentRequest) {

        logger.info("Processing payment of {} {} using Credit Card for email {}",
                paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getEmail());
        // Here we can integrate with PayPal API/SDK
        logger.info("Credit card payment completed successfully");

    }
}
