package com.example.payment.ai.prompt;

public final class PromptTemplates {

    private PromptTemplates() {}

    public static final String PAYMENT_CONTEXT_SECTION = """
            === Recent Payment History ===
            User: {userId}
            Total Transactions: {totalTransactions}
            Recent Payments:
            {paymentSummaries}
            """;

    public static final String PAYMENT_SUMMARY_ENTRY =
            "- [{status}] {transactionId} | {amount} {currency} | {paymentType} | {createdAt}";

    public static final String KNOWLEDGE_CONTEXT_SECTION = """
            === Knowledge Base ===
            {knowledgeExcerpts}
            """;

    public static final String USER_QUERY_SECTION = """
            === User Question ===
            {userQuery}
            """;
}
