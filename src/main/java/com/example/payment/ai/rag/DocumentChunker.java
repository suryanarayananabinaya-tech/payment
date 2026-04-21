package com.example.payment.ai.rag;

import com.example.payment.ai.model.RagChunk;
import com.example.payment.ai.model.RagDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DocumentChunker {

    private static final int CHUNK_SIZE    = 500;
    private static final int CHUNK_OVERLAP = 50;

    public List<RagChunk> chunk(RagDocument document) {
        List<RagChunk> chunks = new ArrayList<>();
        String content = document.getContent();

        if (content == null || content.isBlank()) {
            return chunks;
        }

        int start = 0;
        int index = 0;

        while (start < content.length()) {
            int end = Math.min(start + CHUNK_SIZE, content.length());

            // extend to nearest sentence boundary
            if (end < content.length()) {
                int boundary = lastSentenceBoundary(content, start, end);
                if (boundary > start) {
                    end = boundary;
                }
            }

            chunks.add(RagChunk.builder()
                    .id(UUID.randomUUID().toString())
                    .documentId(document.getId())
                    .content(content.substring(start, end).trim())
                    .chunkIndex(index++)
                    .build());

            start = end - CHUNK_OVERLAP;
        }

        log.debug("Document chunked [documentId={}, chunks={}]", document.getId(), chunks.size());
        return chunks;
    }

    private int lastSentenceBoundary(String text, int start, int end) {
        for (int i = end; i > start; i--) {
            char c = text.charAt(i - 1);
            if (c == '.' || c == '!' || c == '?') {
                return i;
            }
        }
        return end;
    }
}
