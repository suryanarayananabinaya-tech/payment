package com.example.payment.ai.rag;

import com.example.payment.ai.dto.KnowledgeContextDto;
import com.example.payment.ai.exception.RagRetrievalException;
import com.example.payment.ai.model.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagRetriever {

    private static final int TOP_K = 5;
    private static final double MIN_SCORE = 0.7;

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public KnowledgeContextDto retrieve(String query) {
        try {
            log.debug("RAG retrieval started [query length={}]", query.length());

            List<Double> queryEmbedding = embeddingService.embed(query);
            List<RetrievedChunk> chunks = vectorStoreService.search(queryEmbedding, TOP_K);

            List<RetrievedChunk> relevant = chunks.stream()
                    .filter(c -> c.getSimilarityScore() >= MIN_SCORE)
                    .toList();

            List<String> excerpts = relevant.stream()
                    .map(RetrievedChunk::getContent)
                    .toList();

            List<String> sources = relevant.stream()
                    .map(RetrievedChunk::getSource)
                    .distinct()
                    .toList();

            double maxScore = relevant.stream()
                    .mapToDouble(RetrievedChunk::getSimilarityScore)
                    .max()
                    .orElse(0.0);

            log.debug("RAG retrieval complete [relevant={}, maxScore={}]", relevant.size(), maxScore);

            return KnowledgeContextDto.builder()
                    .relevantExcerpts(excerpts)
                    .sourceArticles(sources)
                    .maxRelevanceScore(maxScore)
                    .build();

        } catch (Exception e) {
            log.error("RAG retrieval failed: {}", e.getMessage());
            throw new RagRetrievalException("Failed to retrieve knowledge context", e);
        }
    }
}
