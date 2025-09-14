package com.dawn.cs_study.ai.application.port;

import org.springframework.ai.document.Document;

import java.util.List;

public interface VectorCommandPort {

    void add(List<Document> documents);

    void delete(String id);

}
