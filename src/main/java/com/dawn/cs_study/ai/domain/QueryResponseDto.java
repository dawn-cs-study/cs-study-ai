package com.dawn.cs_study.ai.domain;

import java.util.List;

public record QueryResponseDto(String query, String answer, List<DocumentResponse> relevantDocuments) {
}
