package com.example.payment.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String apiKey;
    private String model = "gpt-3.5-turbo";
    private String baseUrl = "https://api.openai.com/v1";
    private double temperature = 0.7;
    private int maxTokens = 1000;
    private int timeoutSeconds = 30;
    private boolean mockEnabled = true;
}
