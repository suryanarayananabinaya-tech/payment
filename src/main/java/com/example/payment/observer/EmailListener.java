package com.example.payment.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EmailListener {

    private static final Logger logger = LoggerFactory.getLogger(EmailListener.class);

    @EventListener
    public void sendEmail(PaymentEvent event) {
        logger.info("Email sent to {} for amount {}",
                event.getPaymentRequest().getEmail(),
                event.getPaymentRequest().getAmount());
    }
}
