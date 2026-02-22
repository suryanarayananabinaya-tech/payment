package com.example.payment.factory;

import com.example.payment.strategy.CreditCardPayment;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.PaypalPayment;

public class PaymentFactory {


    public static PaymentStrategy getStrategy(String type){

        if (type == null){
            throw new IllegalArgumentException("Payment Type is null");
        }

        return switch (type.toUpperCase()) {
            case "CARD" -> new CreditCardPayment();
            case "NET_BANKING" -> new PaypalPayment();
            default -> throw new IllegalArgumentException("Payment Type is unknown" + type);
        };
    }
}
