package com.example.payment.ai.validator;

import com.example.payment.ai.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiResponseValidator {

    private static final int MAX_RESPONSE_LENGTH = 5000;

    public void validate(String answer) {
        if (answer == null || answer.isBlank()) {
            throw new ValidationException("AI response must not be empty");
        }
        if (answer.length() > MAX_RESPONSE_LENGTH) {
            throw new ValidationException("AI response exceeds maximum length of " + MAX_RESPONSE_LENGTH);
        }

        log.debug("Response validated [length={}]", answer.length());
    }
}
