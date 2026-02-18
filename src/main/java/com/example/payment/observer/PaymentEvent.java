package com.example.payment.observer;

import com.example.payment.model.PaymentRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentEvent  extends ApplicationEvent {

    private final PaymentRequest paymentRequest;

    public PaymentEvent(PaymentRequest paymentRequest) {
        super(paymentRequest);
        this.paymentRequest = paymentRequest;
    }

}
