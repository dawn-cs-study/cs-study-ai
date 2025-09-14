package com.dawn.cs_study.ai.infrastructure.persistence.vector;

import com.dawn.cs_study.ai.application.port.VectorQueryPort;
import com.dawn.cs_study.ai.domain.DocumentSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PgVectorQueryAdapter implements VectorQueryPort {

    private final PgVectorStore pgVectorStore;

    @Override
    public List<Document> similaritySearch(String question, Integer maxResults) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(maxResults)
                .build();

        return pgVectorStore.similaritySearch(searchRequest);
    }
}
