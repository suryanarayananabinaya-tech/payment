package com.example.payment.ai.dto;

import com.example.payment.ai.model.QueryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptInputDto {

    private String userQuery;
    private String userId;
    private String sessionId;
    private QueryType queryType;

    private PaymentContextDto paymentContext;
    private KnowledgeContextDto knowledgeContext;
    private SupportContextDto supportContext;
}
