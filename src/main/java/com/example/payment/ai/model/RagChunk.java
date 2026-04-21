package com.example.payment.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagChunk {

    private String id;
    private String documentId;
    private String content;
    private int chunkIndex;
    private List<Double> embedding;
}
