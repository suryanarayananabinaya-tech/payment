package com.example.payment.ai.rag;

import com.example.payment.ai.config.LlmProperties;
import com.example.payment.ai.exception.RagRetrievalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final LlmProperties llmProperties;
    private final RestClient openAiRestClient;
    private final ObjectMapper objectMapper;

    public List<Double> embed(String text) {
        if (llmProperties.isMockEnabled()) {
            return mockEmbedding(text);
        }
        return callEmbeddingApi(text);
    }

    private List<Double> callEmbeddingApi(String text) {
        log.debug("Calling OpenAI Embeddings API");

        Map<String, Object> requestBody = Map.of(
                "model", "text-embedding-ada-002",
                "input", text
        );

        try {
            String responseJson = openAiRestClient.post()
                    .uri("/embeddings")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseEmbedding(responseJson);

        } catch (Exception e) {
            log.error("Embedding API call failed: {}", e.getMessage());
            throw new RagRetrievalException("Failed to generate embedding", e);
        }
    }

    private List<Double> parseEmbedding(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode values = root.path("data").get(0).path("embedding");
            List<Double> embedding = new ArrayList<>();
            for (JsonNode v : values) {
                embedding.add(v.asDouble());
            }
            return embedding;
        } catch (Exception e) {
            throw new RagRetrievalException("Failed to parse embedding response", e);
        }
    }

    private List<Double> mockEmbedding(String text) {
        log.debug("Mock embedding — returning zero vector [textLength={}]", text.length());
        return List.of(0.0, 0.0, 0.0, 0.0, 0.0);
    }
}
