package com.dawn.cs_study.ai.domain;

import java.util.Map;

public record DocumentSearchResult(String id, String content, Map<String, Object> metadata, Double score) {

    public DocumentResponse toDocumentResponseDto() {
        return new DocumentResponse(
                id, score, content, metadata
        );
    }

}
