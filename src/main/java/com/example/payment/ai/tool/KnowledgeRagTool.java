package com.example.payment.ai.tool;

import com.example.payment.ai.dto.KnowledgeContextDto;
import com.example.payment.ai.model.ToolExecutionResult;
import com.example.payment.ai.rag.RagRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeRagTool implements AgentTool {

    private final RagRetriever ragRetriever;

    @Override
    public String name() {
        return "knowledge_rag";
    }

    @Override
    public String description() {
        return "Search the knowledge base for relevant information. Input: search query.";
    }

    @Override
    public ToolExecutionResult execute(String input) {
        try {
            KnowledgeContextDto context = ragRetriever.retrieve(input);

            List<String> excerpts = context.getRelevantExcerpts();
            String output = (excerpts == null || excerpts.isEmpty())
                    ? "No relevant information found in the knowledge base."
                    : String.join("\n---\n", excerpts);

            log.debug("KnowledgeRagTool executed [query='{}', results={}]",
                    input, excerpts == null ? 0 : excerpts.size());

            return ToolExecutionResult.builder()
                    .output(output)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("KnowledgeRagTool failed [query='{}']: {}", input, e.getMessage());
            return ToolExecutionResult.builder()
                    .success(false)
                    .errorMessage("Failed to search knowledge base: " + e.getMessage())
                    .build();
        }
    }
}
