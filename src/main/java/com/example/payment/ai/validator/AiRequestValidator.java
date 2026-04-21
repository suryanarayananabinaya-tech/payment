package com.example.payment.ai.validator;

import com.example.payment.ai.dto.AiQueryRequest;
import com.example.payment.ai.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiRequestValidator {

    private static final int MAX_QUERY_LENGTH = 1000;

    public void validate(AiQueryRequest request) {
        if (request == null) {
            throw new ValidationException("Request must not be null");
        }
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            throw new ValidationException("Query must not be blank");
        }
        if (request.getQuery().length() > MAX_QUERY_LENGTH) {
            throw new ValidationException("Query exceeds maximum length of " + MAX_QUERY_LENGTH);
        }
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new ValidationException("User ID must not be blank");
        }

        log.debug("Request validated [userId={}]", request.getUserId());
    }
}
