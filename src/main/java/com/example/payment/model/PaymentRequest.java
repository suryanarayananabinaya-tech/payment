package com.example.payment.model;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class PaymentRequest {
    private final Double amount;
    private final String currency;
    private final String email;
    private final String paymentType;

}
