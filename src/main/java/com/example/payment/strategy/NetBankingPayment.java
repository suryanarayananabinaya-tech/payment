package com.example.payment.strategy;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NetBankingPayment implements TypedPaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(NetBankingPayment.class);


    @Override
    public PaymentType getType() {
        return PaymentType.NET_BANKING;
    }

    @Override
    public void pay(PaymentRequest paymentRequest) {

        logger.info("Processing payment of {} {} using PAYPAL for email {}",
                paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getEmail());
        // Here we  can integrate with PayPal API/SDK
        logger.info("PayPal payment completed successfully");
    }
}
