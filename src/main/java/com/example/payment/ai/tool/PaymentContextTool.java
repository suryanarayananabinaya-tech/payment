package com.example.payment.ai.tool;

import com.example.payment.ai.dto.PaymentContextDto;
import com.example.payment.ai.model.ToolExecutionResult;
import com.example.payment.ai.retriever.PaymentContextRetriever;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentContextTool implements AgentTool {

    private final PaymentContextRetriever paymentContextRetriever;
    private final ObjectMapper objectMapper;

    @Override
    public String name() {
        return "payment_context";
    }

    @Override
    public String description() {
        return "Retrieve the user's recent payment history. Input: username.";
    }

    @Override
    public ToolExecutionResult execute(String input) {
        try {
            PaymentContextDto context = paymentContextRetriever.retrieve(input.trim());
            String output = objectMapper.writeValueAsString(context);
            log.debug("PaymentContextTool executed [username={}]", input.trim());
            return ToolExecutionResult.builder()
                    .output(output)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("PaymentContextTool failed [username={}]: {}", input, e.getMessage());
            return ToolExecutionResult.builder()
                    .success(false)
                    .errorMessage("Failed to retrieve payment context: " + e.getMessage())
                    .build();
        }
    }
}
