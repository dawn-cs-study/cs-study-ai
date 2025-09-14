package com.dawn.cs_study.ai.infrastructure.persistence.vector;

import com.dawn.cs_study.ai.application.port.VectorCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PgVectorCommandAdapter implements VectorCommandPort {

    private final PgVectorStore pgVectorStore;

    @Override
    public void add(List<Document> documents) {
        pgVectorStore.add(documents);
    }

    @Override
    public void delete(String id) {
        pgVectorStore.delete("key =='" + id + "'");
    }
}
