package com.example.payment.ai.validator;

import com.example.payment.ai.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class PromptSafetyValidator {

    private static final Set<String> INJECTION_PATTERNS = Set.of(
            "ignore previous instructions",
            "ignore all instructions",
            "disregard your instructions",
            "you are now",
            "act as",
            "forget everything",
            "jailbreak",
            "dan mode",
            "developer mode"
    );

    public void validate(String query) {
        if (query == null || query.isBlank()) {
            return;
        }

        String normalized = query.toLowerCase().trim();

        for (String pattern : INJECTION_PATTERNS) {
            if (normalized.contains(pattern)) {
                log.warn("Prompt injection attempt detected [pattern='{}']", pattern);
                throw new ValidationException("Query contains disallowed content");
            }
        }

        log.debug("Prompt safety check passed");
    }
}
