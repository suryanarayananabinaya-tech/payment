package com.example.payment.ai.workflow;

import com.example.payment.ai.dto.AiQueryRequest;
import com.example.payment.ai.dto.KnowledgeContextDto;
import com.example.payment.ai.dto.PaymentContextDto;
import com.example.payment.ai.model.QueryType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiWorkflowContext {

    private AiQueryRequest request;
    private String sessionId;
    private String traceId;

    private QueryType queryType;
    private String assembledPrompt;
    private String rawAnswer;
    private KnowledgeContextDto knowledgeContext;

    private AiWorkflowStep currentStep;
    private boolean failed;
    private String failureReason;

    private PaymentContextDto paymentContext;
    private String systemPrompt;
}
