package com.example.payment.ai.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentFinalResponse {

    private String answer;
    private int totalSteps;
    private boolean success;
}
