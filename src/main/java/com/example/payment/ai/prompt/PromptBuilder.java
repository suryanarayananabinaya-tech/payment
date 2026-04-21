package com.example.payment.ai.prompt;

import com.example.payment.ai.dto.PaymentContextDto;
import com.example.payment.ai.dto.PromptInputDto;
import com.example.payment.ai.model.QueryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
public class PromptBuilder {

    public String buildSystemPrompt(QueryType queryType) {
        return switch (queryType) {
            case PAYMENT_INQUIRY  -> SystemPrompts.PAYMENT_INQUIRY;
            case PAYMENT_FAILURE  -> SystemPrompts.PAYMENT_FAILURE;
            case REFUND_REQUEST   -> SystemPrompts.REFUND_REQUEST;
            case ACCOUNT_INQUIRY  -> SystemPrompts.ACCOUNT_INQUIRY;
            case SUPPORT_REQUEST  -> SystemPrompts.SUPPORT_REQUEST;
            case GENERAL_FAQ      -> SystemPrompts.GENERAL_FAQ;
            default               -> SystemPrompts.UNKNOWN;
        };
    }

    public String buildUserPrompt(PromptInputDto input) {
        StringBuilder prompt = new StringBuilder();

        if (input.getPaymentContext() != null
                && input.getPaymentContext().getRecentPayments() != null
                && !input.getPaymentContext().getRecentPayments().isEmpty()) {
            prompt.append(buildPaymentContextSection(input.getPaymentContext()));
        }

        if (input.getKnowledgeContext() != null
                && input.getKnowledgeContext().getRelevantExcerpts() != null
                && !input.getKnowledgeContext().getRelevantExcerpts().isEmpty()) {
            prompt.append(buildKnowledgeContextSection(input));
        }

        prompt.append(PromptTemplates.USER_QUERY_SECTION
                .replace("{userQuery}", input.getUserQuery()));

        log.debug("User prompt built [userId={}, length={}]",
                input.getUserId(), prompt.length());

        return prompt.toString();
    }

    private String buildPaymentContextSection(PaymentContextDto ctx) {
        String summaries = ctx.getRecentPayments().stream()
                .map(p -> PromptTemplates.PAYMENT_SUMMARY_ENTRY
                        .replace("{status}",        p.getStatus())
                        .replace("{transactionId}", p.getTransactionId())
                        .replace("{amount}",        p.getAmount().toPlainString())
                        .replace("{currency}",      p.getCurrency())
                        .replace("{paymentType}",   p.getPaymentType())
                        .replace("{createdAt}",     p.getCreatedAt().toString()))
                .collect(Collectors.joining("\n"));

        return PromptTemplates.PAYMENT_CONTEXT_SECTION
                .replace("{userId}",            ctx.getUserId())
                .replace("{totalTransactions}", String.valueOf(ctx.getTotalTransactions()))
                .replace("{paymentSummaries}",  summaries);
    }

    private String buildKnowledgeContextSection(PromptInputDto input) {
        String excerpts = String.join("\n", input.getKnowledgeContext().getRelevantExcerpts());
        return PromptTemplates.KNOWLEDGE_CONTEXT_SECTION
                .replace("{knowledgeExcerpts}", excerpts);
    }
}
