package com.dawn.cs_study.ai.domain;

import java.util.List;

public record QueryResult(String query, String answer, List<DocumentResult> relevantDocuments) {

}
