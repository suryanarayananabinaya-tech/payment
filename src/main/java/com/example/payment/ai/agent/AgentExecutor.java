package com.example.payment.ai.agent;

import com.example.payment.ai.llm.LlmClient;
import com.example.payment.ai.model.LlmResponse;
import com.example.payment.ai.model.ToolExecutionResult;
import com.example.payment.ai.tool.AgentTool;
import com.example.payment.ai.tool.FinalAnswerTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentExecutor {

    private final LlmClient llmClient;
    private final AgentToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public AgentStepResult step(AgentState state) {
        String userPrompt = buildStepPrompt(state);

        LlmResponse llmResponse = llmClient.complete(state.getSystemPrompt(), userPrompt);
        String content = llmResponse.getChoices().get(0).getMessage().getContent();

        log.debug("LLM response at step {} [sessionId={}]: {}",
                state.getCurrentStep(), state.getSessionId(), content);

        AgentAction action = parseAction(content);
        boolean isFinal = FinalAnswerTool.TOOL_NAME.equals(action.getToolName());

        AgentTool tool = toolRegistry.get(action.getToolName());
        ToolExecutionResult result = tool.execute(action.getToolInput());

        AgentObservation observation = AgentObservation.builder()
                .toolName(action.getToolName())
                .output(result.isSuccess() ? result.getOutput() : result.getErrorMessage())
                .success(result.isSuccess())
                .build();

        state.addObservation(observation);

        if (isFinal) {
            state.setFinalAnswer(action.getToolInput());
        }

        log.debug("Step {} complete [tool={}, final={}]",
                state.getCurrentStep(), action.getToolName(), isFinal);

        return AgentStepResult.builder()
                .action(action)
                .observation(observation)
                .isFinal(isFinal)
                .build();
    }

    private String buildStepPrompt(AgentState state) {
        StringBuilder sb = new StringBuilder();

        sb.append("Available tools:\n")
                .append(toolRegistry.describeTools())
                .append("\n\n");

        sb.append("Always respond with a JSON object in this exact format:\n")
                .append("{\"tool\": \"<tool_name>\", \"input\": \"<tool_input>\"}\n\n");

        sb.append("User query: ").append(state.getQuery()).append("\n");

        if (!state.getHistory().isEmpty()) {
            sb.append("\nPrevious steps:\n");
            for (AgentObservation obs : state.getHistory()) {
                sb.append("Tool used: ").append(obs.getToolName()).append("\n");
                sb.append("Result: ").append(obs.getOutput()).append("\n\n");
            }
            sb.append("Based on the above, what is the next step?\n");
        }

        return sb.toString();
    }

    private AgentAction parseAction(String content) {
        try {
            int start = content.indexOf('{');
            int end   = content.lastIndexOf('}');

            if (start == -1 || end == -1 || end <= start) {
                return defaultFinalAnswer(content);
            }

            JsonNode node = objectMapper.readTree(content.substring(start, end + 1));
            String toolName  = node.path("tool").asText();
            String toolInput = node.path("input").asText();

            if (toolName.isBlank()) {
                return defaultFinalAnswer(content);
            }

            return AgentAction.builder()
                    .toolName(toolName)
                    .toolInput(toolInput)
                    .build();

        } catch (Exception e) {
            log.warn("Could not parse LLM action — defaulting to final_answer");
            return defaultFinalAnswer(content);
        }
    }

    private AgentAction defaultFinalAnswer(String content) {
        return AgentAction.builder()
                .toolName(FinalAnswerTool.TOOL_NAME)
                .toolInput(content)
                .build();
    }
}
