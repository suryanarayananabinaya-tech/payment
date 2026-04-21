package com.example.payment.ai.mapper;

import com.example.payment.ai.exception.LlmIntegrationException;
import com.example.payment.ai.model.InternalAiResponse;
import com.example.payment.ai.model.LlmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LlmResponseMapper {

    public InternalAiResponse toInternal(LlmResponse llmResponse) {
        if (llmResponse.getChoices() == null || llmResponse.getChoices().isEmpty()) {
            throw new LlmIntegrationException("LLM response contained no choices");
        }

        LlmResponse.Choice choice = llmResponse.getChoices().get(0);
        String content = choice.getMessage() != null ? choice.getMessage().getContent() : null;

        if (content == null || content.isBlank()) {
            throw new LlmIntegrationException("LLM response content is empty");
        }

        int promptTokens     = llmResponse.getUsage() != null ? llmResponse.getUsage().getPromptTokens()     : 0;
        int completionTokens = llmResponse.getUsage() != null ? llmResponse.getUsage().getCompletionTokens() : 0;

        log.debug("LLM response mapped [finishReason={}, tokens={}/{}]",
                choice.getFinishReason(), promptTokens, completionTokens);

        return InternalAiResponse.builder()
                .answer(content)
                .finishReason(choice.getFinishReason())
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .build();
    }
}
