package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@AllArgsConstructor
public class PaymentResponseDTO {


    private String status;
    private String message;
    private double amount;
    private String paymentType;


}
