package com.example.payment.ai.agent;

import com.example.payment.ai.workflow.AiWorkflowContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentOrchestrator {

    private static final int MAX_STEPS = 5;

    private final AgentExecutor agentExecutor;

    public AgentFinalResponse run(AiWorkflowContext context) {
        AgentState state = AgentState.builder()
                .query(context.getRequest().getQuery())
                .userId(context.getRequest().getUserId())
                .sessionId(context.getSessionId())
                .systemPrompt(context.getSystemPrompt())
                .contextPrompt(context.getAssembledPrompt())
                .history(new ArrayList<>())
                .currentStep(0)
                .build();

        log.info("Agent started [sessionId={}, userId={}]",
                state.getSessionId(), state.getUserId());

        for (int step = 0; step < MAX_STEPS; step++) {
            state.setCurrentStep(step + 1);

            log.debug("Agent step {}/{} [sessionId={}]",
                    step + 1, MAX_STEPS, state.getSessionId());

            AgentStepResult result = agentExecutor.step(state);

            if (result.isFinal()) {
                log.info("Agent completed in {} step(s) [sessionId={}]",
                        step + 1, state.getSessionId());
                return AgentFinalResponse.builder()
                        .answer(state.getFinalAnswer())
                        .totalSteps(step + 1)
                        .success(true)
                        .build();
            }
        }

        log.warn("Agent reached max steps ({}) without final answer [sessionId={}]",
                MAX_STEPS, state.getSessionId());

        return AgentFinalResponse.builder()
                .answer("I was unable to fully process your request. Please try again.")
                .totalSteps(MAX_STEPS)
                .success(false)
                .build();
    }
}