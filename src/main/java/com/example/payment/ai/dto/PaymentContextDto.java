package com.example.payment.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentContextDto {

    private String userId;
    private int totalTransactions;
    private List<PaymentSummary> recentPayments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummary {
        private String transactionId;
        private BigDecimal amount;
        private String currency;
        private String paymentType;
        private String status;
        private LocalDateTime createdAt;
    }
}
