package com.example.payment.ai.llm;

import com.example.payment.ai.config.LlmProperties;
import com.example.payment.ai.exception.LlmIntegrationException;
import com.example.payment.ai.model.LlmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiLlmClient implements LlmClient {

    private final LlmProperties llmProperties;
    private final LlmResponseParser responseParser;
    private final RestClient openAiRestClient;

    @Override
    public LlmResponse complete(String systemPrompt, String userPrompt) {
        if (llmProperties.isMockEnabled()) {
            return mockResponse(userPrompt);
        }
        return callOpenAi(systemPrompt, userPrompt);
    }

    private LlmResponse callOpenAi(String systemPrompt, String userPrompt) {
        log.debug("Calling OpenAI API [model={}]", llmProperties.getModel());

        Map<String, Object> requestBody = Map.of(
                "model", llmProperties.getModel(),
                "temperature", llmProperties.getTemperature(),
                "max_tokens", llmProperties.getMaxTokens(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user",   "content", userPrompt)
                )
        );

        try {
            String responseJson = openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return responseParser.parse(responseJson);

        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            throw new LlmIntegrationException("OpenAI API call failed", e);
        }
    }

    private LlmResponse mockResponse(String userPrompt) {
        log.info("Mock LLM enabled — returning stub response");

        String content = "This is a mock AI response for: \"" + userPrompt + "\". "
                + "Configure llm.mock-enabled=false and set llm.api-key to use the real OpenAI API.";

        return LlmResponse.builder()
                .id(UUID.randomUUID().toString())
                .model("mock")
                .choices(List.of(new LlmResponse.Choice(
                        0,
                        new LlmResponse.Message("assistant", content),
                        "stop"
                )))
                .usage(new LlmResponse.Usage(0, 0, 0))
                .build();
    }
}
