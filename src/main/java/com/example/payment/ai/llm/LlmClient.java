package com.example.payment.ai.llm;

import com.example.payment.ai.model.LlmResponse;

public interface LlmClient {

    LlmResponse complete(String systemPrompt, String userPrompt);
}
