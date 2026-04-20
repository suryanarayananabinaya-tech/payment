package com.example.payment.ai.rag;

import com.example.payment.ai.model.RagChunk;
import com.example.payment.ai.model.RetrievedChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class VectorStoreService {

    // In-memory store — replace with pgvector / Pinecone / Weaviate when ready
    private final ConcurrentHashMap<String, RagChunk> store = new ConcurrentHashMap<>();

    public void save(RagChunk chunk) {
        store.put(chunk.getId(), chunk);
        log.debug("Chunk saved to vector store [chunkId={}, documentId={}]",
                chunk.getId(), chunk.getDocumentId());
    }

    public void saveAll(List<RagChunk> chunks) {
        chunks.forEach(this::save);
        log.debug("Saved {} chunks to vector store", chunks.size());
    }

    public List<RetrievedChunk> search(List<Double> queryEmbedding, int topK) {
        if (store.isEmpty()) {
            log.debug("Vector store is empty — returning no results");
            return List.of();
        }

        List<RetrievedChunk> results = new ArrayList<>();

        for (RagChunk chunk : store.values()) {
            if (chunk.getEmbedding() == null || chunk.getEmbedding().isEmpty()) {
                continue;
            }
            double score = cosineSimilarity(queryEmbedding, chunk.getEmbedding());
            results.add(RetrievedChunk.builder()
                    .content(chunk.getContent())
                    .source(chunk.getDocumentId())
                    .similarityScore(score)
                    .build());
        }

        results.sort(Comparator.comparingDouble(RetrievedChunk::getSimilarityScore).reversed());

        List<RetrievedChunk> topResults = results.stream().limit(topK).toList();
        log.debug("Vector search complete [hits={}, topK={}]", topResults.size(), topK);
        return topResults;
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) return 0.0;

        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dot   += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
