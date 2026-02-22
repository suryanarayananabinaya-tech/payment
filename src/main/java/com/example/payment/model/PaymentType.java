package com.example.payment.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentType {
    CARD,
    NET_BANKING;

    @JsonCreator
    public static PaymentType from(String value) {
        return PaymentType.valueOf(value.trim().toUpperCase());
    }
}