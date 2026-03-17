package com.example.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {
    private String eventId;
    private String transactionId;
    private String userName;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    private String status;
    private LocalDateTime createdAt;
    private String traceId;
}