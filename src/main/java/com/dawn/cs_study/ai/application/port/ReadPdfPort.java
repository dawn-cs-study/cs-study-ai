package com.dawn.cs_study.ai.application.port;

import org.springframework.ai.document.Document;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ReadPdfPort {

    List<Document> readPdfAsDocuments(Path path, Map<String, Object> baseMeta);

}
