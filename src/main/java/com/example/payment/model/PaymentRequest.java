package com.example.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class PaymentRequest {


    private  BigDecimal amount;
    private  String currency;
    private  String email;
    private  PaymentType paymentType; // Use enum instead of String
    private  String userId;

    // Enum for safer payment types
    public enum PaymentType {
        NET_BANKING,
        CARD
    }

}
