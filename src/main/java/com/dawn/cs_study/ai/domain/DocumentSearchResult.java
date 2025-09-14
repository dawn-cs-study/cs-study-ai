package com.dawn.cs_study.ai.domain;

import java.util.Map;

public record DocumentSearchResult(String id, String content, Map<String, Object> metadata, Double score) {

    public DocumentResult toDocumentResult() {
        return new DocumentResult(
                id, score, content, metadata
        );
    }

}
