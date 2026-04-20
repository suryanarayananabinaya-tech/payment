package com.example.payment.ai.service;

import com.example.payment.ai.dto.AiQueryRequest;
import com.example.payment.ai.dto.AiQueryResponse;
import com.example.payment.ai.exception.AiProcessingException;
import com.example.payment.ai.model.QueryType;
import com.example.payment.ai.workflow.AiWorkflowEngine;
import com.example.payment.ai.workflow.AiWorkflowResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AiSupportService {

    private final AiWorkflowEngine workflowEngine;

    public AiSupportService(AiWorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    public AiQueryResponse handle(AiQueryRequest request) {

        String traceId = UUID.randomUUID().toString();
        String sessionId = resolveSessionId(request.getSessionId());

        log.info("AI query received [traceId={}, userId={}, sessionId={}]",
                traceId, request.getUserId(), sessionId);

        try {
            String mode = request.getMode();
            AiWorkflowResult result;
            if ("AGENT".equalsIgnoreCase(mode)) {
                result = workflowEngine.executeAgentWorkflow(request, sessionId, traceId);
            } else {
                result = workflowEngine.executeWorkflow(request, sessionId, traceId);
            }
            String answer = result.getAnswer();
            QueryType queryType = result.getQueryType();

            log.info("AI query completed [traceId={}, queryType={}]", traceId, queryType);

            return AiQueryResponse.builder()
                    .answer(answer)
                    .sessionId(sessionId)
                    .traceId(traceId)
                    .queryType(queryType != null ? queryType.name() : "UNKNOWN")
                    .sources(List.of())
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (AiProcessingException e) {
            log.error("AI processing failed [traceId={}]: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing AI query [traceId={}]", traceId, e);
            throw new AiProcessingException("Failed to process AI query", e);
        }
    }

    private String resolveSessionId(String sessionId) {
        return (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();
    }
}