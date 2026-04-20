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

        log.info("AI query received [traceId={}, userId={}, sessionId={}, mode={}]",
                traceId, request.getUserId(), sessionId, request.getMode());

        try {
            AiWorkflowResult result = executeByMode(request, sessionId, traceId);

            if (!result.isSuccess()) {
                log.warn("AI query completed with failure [traceId={}, queryType={}, reason={}]",
                        traceId, result.getQueryType(), result.getFailureReason());
            } else {
                log.info("AI query completed successfully [traceId={}, queryType={}]",
                        traceId, result.getQueryType());
            }

            QueryType queryType = result.getQueryType();

            return AiQueryResponse.builder()
                    .answer(result.getAnswer())
                    .sessionId(result.getSessionId())
                    .traceId(result.getTraceId())
                    .queryType(queryType != null ? queryType.name() : "UNKNOWN")
                    .sources(result.getSources() != null ? result.getSources() : List.of())
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (AiProcessingException e) {
            log.error("AI processing failed [traceId={}]: {}", traceId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing AI query [traceId={}]", traceId, e);
            throw new AiProcessingException("Failed to process AI query", e);
        }
    }

    private AiWorkflowResult executeByMode(AiQueryRequest request, String sessionId, String traceId) {
        String mode = request.getMode();

        if ("AGENT".equalsIgnoreCase(mode)) {
            return workflowEngine.executeAgentWorkflow(request, sessionId, traceId);
        }

        return workflowEngine.executeWorkflow(request, sessionId, traceId);
    }

    private String resolveSessionId(String sessionId) {
        return (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();
    }
}