package com.example.payment.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQueryResponse {

    private String answer;
    private String sessionId;
    private String traceId;
    private String queryType;
    private List<String> sources;
    private LocalDateTime timestamp;
}
