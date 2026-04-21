package com.example.payment.ai.controller;

import com.example.payment.ai.dto.AiQueryRequest;
import com.example.payment.ai.dto.AiQueryResponse;
import com.example.payment.ai.service.AiSupportService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai/support")
public class AiSupportController {

    private final AiSupportService aiSupportService;

    public AiSupportController(AiSupportService aiSupportService) {
        this.aiSupportService = aiSupportService;
    }

    // WORKFLOW mode — RAG pipeline + direct LLM (default)
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<AiQueryResponse> ask(@Valid @RequestBody AiQueryRequest request) {
        AiQueryRequest workflowRequest = toMode(request, "WORKFLOW");
        log.info("AI request received [mode=WORKFLOW, userId={}]", request.getUserId());
        return ResponseEntity.ok(aiSupportService.handle(workflowRequest));
    }

    // AGENT mode — classify → agent loop with tool calls
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/agent")
    public ResponseEntity<AiQueryResponse> askAgent(@Valid @RequestBody AiQueryRequest request) {
        AiQueryRequest agentRequest = toMode(request, "AGENT");
        log.info("AI request received [mode=AGENT, userId={}]", request.getUserId());
        return ResponseEntity.ok(aiSupportService.handle(agentRequest));
    }

    private AiQueryRequest toMode(AiQueryRequest request, String mode) {
        return AiQueryRequest.builder()
                .query(request.getQuery())
                .userId(request.getUserId())
                .sessionId(request.getSessionId())
                .mode(mode)
                .build();
    }
}
