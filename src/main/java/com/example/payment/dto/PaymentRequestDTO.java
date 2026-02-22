package com.example.payment.dto;


import com.example.payment.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class PaymentRequestDTO {
    @NotNull
    @Positive
    private BigDecimal amount;
    @NotNull
    private String currency;
    @NotNull
    private PaymentType paymentType;
    @NotNull
    private String email;





}
