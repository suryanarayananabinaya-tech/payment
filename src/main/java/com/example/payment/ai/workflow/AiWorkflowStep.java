package com.example.payment.ai.workflow;

public enum AiWorkflowStep {
    VALIDATE_INPUT,
    CLASSIFY_QUERY,
    RETRIEVE_CONTEXT,
    BUILD_PROMPT,
    RUN_LLM,
    RUN_AGENT,
    VALIDATE_RESPONSE,
    COMPLETE
}
