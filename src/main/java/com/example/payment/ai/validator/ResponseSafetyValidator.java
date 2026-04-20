package com.example.payment.ai.validator;

import com.example.payment.ai.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ResponseSafetyValidator {

    private static final Set<String> BLOCKED_PHRASES = Set.of(
            "ignore previous instructions",
            "as an ai language model, i cannot",
            "i am not able to provide",
            "my instructions are",
            "system prompt",
            "confidential",
            "internal use only"
    );

    private static final Pattern CARD_NUMBER_PATTERN =
            Pattern.compile("\\b\\d{4}[\\s\\-]?\\d{4}[\\s\\-]?\\d{4}[\\s\\-]?\\d{4}\\b");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private static final Pattern IBAN_PATTERN =
            Pattern.compile("\\b[A-Z]{2}\\d{2}[A-Z0-9]{4}\\d{7}([A-Z0-9]?){0,16}\\b");

    public void validate(String response) {
        if (response == null || response.isBlank()) {
            throw new ValidationException("Response must not be empty");
        }

        String normalized = response.toLowerCase();

        checkBlockedPhrases(normalized);
        checkPiiLeakage(response);

        log.debug("Response safety check passed [length={}]", response.length());
    }

    private void checkBlockedPhrases(String normalized) {
        for (String phrase : BLOCKED_PHRASES) {
            if (normalized.contains(phrase)) {
                log.warn("Blocked phrase detected in response [phrase='{}']", phrase);
                throw new ValidationException("Response contains disallowed content");
            }
        }
    }

    private void checkPiiLeakage(String response) {
        if (CARD_NUMBER_PATTERN.matcher(response).find()) {
            log.warn("Card number pattern detected in response");
            throw new ValidationException("Response contains sensitive payment data");
        }
        if (IBAN_PATTERN.matcher(response).find()) {
            log.warn("IBAN pattern detected in response");
            throw new ValidationException("Response contains sensitive payment data");
        }
        if (EMAIL_PATTERN.matcher(response).find()) {
            log.warn("Email address detected in response");
            throw new ValidationException("Response contains personally identifiable information");
        }
    }
}
