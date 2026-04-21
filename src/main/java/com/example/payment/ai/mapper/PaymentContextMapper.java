package com.example.payment.ai.mapper;

import com.example.payment.ai.dto.PaymentContextDto;
import com.example.payment.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentContextMapper {

    public PaymentContextDto toDto(String userId, List<Payment> payments) {
        List<PaymentContextDto.PaymentSummary> summaries = payments.stream()
                .map(this::toSummary)
                .toList();

        return PaymentContextDto.builder()
                .userId(userId)
                .totalTransactions(summaries.size())
                .recentPayments(summaries)
                .build();
    }

    private PaymentContextDto.PaymentSummary toSummary(Payment payment) {
        return PaymentContextDto.PaymentSummary.builder()
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentType(payment.getPaymentType().name())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
