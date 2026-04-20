package com.example.payment.ai.tool;

import com.example.payment.ai.model.ToolExecutionResult;

public interface AgentTool {

    String name();

    String description();

    ToolExecutionResult execute(String input);
}
