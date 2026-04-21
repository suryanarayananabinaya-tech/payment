package com.example.payment.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeContextDto {

    private List<String> relevantExcerpts;
    private List<String> sourceArticles;
    private double maxRelevanceScore;
}
