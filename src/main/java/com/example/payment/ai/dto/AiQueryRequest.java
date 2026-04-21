package com.example.payment.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQueryRequest {

    @NotBlank(message = "Query must not be blank")
    @Size(max = 1000, message = "Query must not exceed 1000 characters")
    private String query;

    @NotBlank(message = "User ID must not be blank")
    private String userId;

    private String sessionId;

    @Builder.Default
    private String mode = "WORKFLOW";
}
