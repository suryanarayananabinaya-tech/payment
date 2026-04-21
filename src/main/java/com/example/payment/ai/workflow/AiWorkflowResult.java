package com.example.payment.ai.workflow;

import com.example.payment.ai.model.QueryType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiWorkflowResult {

    private String answer;
    private String sessionId;
    private String traceId;
    private QueryType queryType;
    private List<String> sources;
    private boolean success;
    private String failureReason;
}
