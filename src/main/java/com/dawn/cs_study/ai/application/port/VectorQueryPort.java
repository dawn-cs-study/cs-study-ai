package com.dawn.cs_study.ai.application.port;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;

public interface VectorQueryPort {

    List<Document> similaritySearch(String question, Integer maxResults);

}
