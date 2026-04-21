package com.example.payment.ai.classifier;

import com.example.payment.ai.model.QueryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class QueryClassifier {

    private static final Map<QueryType, Set<String>> KEYWORDS = Map.of(
            QueryType.PAYMENT_INQUIRY,  Set.of("payment", "transaction", "charge", "paid", "debit", "transfer"),
            QueryType.PAYMENT_FAILURE,  Set.of("failed", "declined", "rejected", "error", "unsuccessful", "not processed"),
            QueryType.REFUND_REQUEST,   Set.of("refund", "money back", "return", "revert", "reversal", "chargeback"),
            QueryType.ACCOUNT_INQUIRY,  Set.of("account", "balance", "profile", "details", "statement", "history"),
            QueryType.SUPPORT_REQUEST,  Set.of("help", "support", "issue", "problem", "complaint", "contact"),
            QueryType.GENERAL_FAQ,      Set.of("how", "what", "when", "why", "can i", "do you", "is it")
    );

    public QueryType classify(String query) {
        if (query == null || query.isBlank()) {
            return QueryType.UNKNOWN;
        }

        String normalized = query.toLowerCase().trim();

        for (Map.Entry<QueryType, Set<String>> entry : KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalized.contains(keyword)) {
                    log.debug("Query classified as {} [keyword='{}']", entry.getKey(), keyword);
                    return entry.getKey();
                }
            }
        }

        log.debug("Query could not be classified, defaulting to UNKNOWN");
        return QueryType.UNKNOWN;
    }
}
