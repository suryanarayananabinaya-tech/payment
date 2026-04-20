package com.example.payment.ai.tool;

import com.example.payment.ai.model.ToolExecutionResult;
import org.springframework.stereotype.Component;

@Component
public class FinalAnswerTool implements AgentTool {

    public static final String TOOL_NAME = "final_answer";

    @Override
    public String name() {
        return TOOL_NAME;
    }

    @Override
    public String description() {
        return "Provide the final answer to the user. Input: the complete answer text.";
    }

    @Override
    public ToolExecutionResult execute(String input) {
        return ToolExecutionResult.builder()
                .output(input)
                .success(true)
                .build();
    }
}
