package com.example.payment.factory;

import com.example.payment.dto.PaymentRequestDTO;
import com.example.payment.strategy.CreditCardPayment;
import com.example.payment.strategy.PaymentStrategy;
import com.example.payment.strategy.PaypalPayment;

public class PaymentFactory {


    public static PaymentStrategy getStrategy(String type){

        if (type == null){
            throw new IllegalArgumentException("Payment Type is null");
        }

        return switch (type.toUpperCase()) {
            case "CREDIT" -> new CreditCardPayment();
            case "PAYPAL" -> new PaypalPayment();
            default -> throw new IllegalArgumentException("Payment Type is unknown" + type);
        };
    }
}
