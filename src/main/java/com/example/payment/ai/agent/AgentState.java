package com.example.payment.ai.agent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AgentState {

    private String query;
    private String userId;
    private String sessionId;
    private String systemPrompt;
    private List<AgentObservation> history;
    private int currentStep;
    private String finalAnswer;

    public void addObservation(AgentObservation observation) {
        history.add(observation);
    }
}
