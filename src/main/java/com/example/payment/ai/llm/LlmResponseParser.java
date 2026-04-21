package com.example.payment.ai.llm;

import com.example.payment.ai.exception.LlmIntegrationException;
import com.example.payment.ai.model.LlmResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmResponseParser {

    private final ObjectMapper objectMapper;

    public LlmResponse parse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            String id    = root.path("id").asText();
            String model = root.path("model").asText();

            List<LlmResponse.Choice> choices = new ArrayList<>();
            for (JsonNode choiceNode : root.path("choices")) {
                JsonNode messageNode = choiceNode.path("message");
                LlmResponse.Message message = new LlmResponse.Message(
                        messageNode.path("role").asText(),
                        messageNode.path("content").asText()
                );
                choices.add(new LlmResponse.Choice(
                        choiceNode.path("index").asInt(),
                        message,
                        choiceNode.path("finish_reason").asText()
                ));
            }

            JsonNode usageNode = root.path("usage");
            LlmResponse.Usage usage = new LlmResponse.Usage(
                    usageNode.path("prompt_tokens").asInt(),
                    usageNode.path("completion_tokens").asInt(),
                    usageNode.path("total_tokens").asInt()
            );

            return LlmResponse.builder()
                    .id(id)
                    .model(model)
                    .choices(choices)
                    .usage(usage)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", e.getMessage());
            throw new LlmIntegrationException("Failed to parse LLM response", e);
        }
    }
}
