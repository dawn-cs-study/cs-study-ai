package com.dawn.cs_study.ai.infrastructure.pdf;

import com.dawn.cs_study.ai.application.port.ReadPdfPort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReadPdfAdapter implements ReadPdfPort {
    @Override
    public List<Document> readPdfAsDocuments(Path path, Map<String, Object> baseMeta) {
        try {
            var para = new ParagraphPdfDocumentReader(new FileSystemResource(path.toFile()));
            var r = para.read();
            if (!r.isEmpty()) return withMetaAndNormalize(r, baseMeta);
        } catch (Exception ignored) {
        }

        var page = new PagePdfDocumentReader(new FileSystemResource(path.toFile()));
        var r = page.read();
        return withMetaAndNormalize(r, baseMeta);
    }

    private List<Document> withMetaAndNormalize(List<Document> docs, Map<String, Object> baseMeta) {
        List<Document> normalized = new ArrayList<>();
        for (Document d : docs) {
            String content = d.getText();
            if (content != null) {
                // 불필요한 공백/탭 → 하나의 공백으로 축소
                String normalizedContent = content
                        .replaceAll("[ \\t]+", " ")
                        // 줄 시작/끝 공백 제거
                        .replaceAll("(?m)^\\s+", "")
                        // 2줄 이상 개행은 두 줄로 축소
                        .replaceAll("(?m)(\\n\\s*){2,}", "\n\n")
                        .trim();

                Document newDoc = new Document(normalizedContent, new HashMap<>(d.getMetadata()));
                newDoc.getMetadata().putAll(baseMeta); // 공통 메타데이터 병합
                normalized.add(newDoc);
            }
        }
        return normalized;
    }
}
