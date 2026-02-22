package com.example.payment.dto;

import com.example.payment.model.PaymentStatus;
import com.example.payment.model.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {


    private PaymentStatus status;
    private String message;

    private BigDecimal amount;
    private String currency;

    private PaymentType paymentType;
    private String transactionId;



}
