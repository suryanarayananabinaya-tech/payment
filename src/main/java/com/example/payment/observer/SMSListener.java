package com.example.payment.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SMSListener {

    private static final Logger logger = LoggerFactory.getLogger(SMSListener.class);

    @EventListener
    public void sendSMS(PaymentEvent event) {
        logger.info("SMS sent for amount {} to {}",
                event.getPaymentRequest().getEmail(), event.getPaymentRequest().getAmount());
    }
}
