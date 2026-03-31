package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RateLimitResponseDTO {
    private String errorCode;
    private String message;
}
