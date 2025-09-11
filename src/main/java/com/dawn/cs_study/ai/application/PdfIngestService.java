package com.dawn.cs_study.ai.application;// PdfIngestService.java

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;

@Service
public class PdfIngestService {

    private final PgVectorStore store;

    public PdfIngestService(PgVectorStore store) {
        this.store = store;
    }

    /**
     * PDF 업로드를 받아 pgvector에 저장
     */
    public String ingest(MultipartFile file) {
        String id = UUID.randomUUID().toString();


        try {
            // 0) 임시 파일 저장

            String original = Objects.requireNonNullElse(file.getOriginalFilename(), "upload.pdf");

            String uploadDir = "/Users/seungjae/Desktop/fs";

            Path temp = Files.createTempFile(Path.of(uploadDir), "upload_", original);
            File tempFile = temp.toFile();

            // 저장
            file.transferTo(tempFile);

            // 1) PDF → Document (문단→페이지 폴백)
            Map<String, Object> baseMeta = Map.of(
                    "id", id,
                    "filename", original
            );
            List<Document> docs = readPdfAsDocuments(temp, baseMeta);

            // 2) Split (토큰 단위)
            var splitter = TokenTextSplitter.builder()
                    .withChunkSize(900)
                    .withMinChunkSizeChars(400)
                    .withMinChunkLengthToEmbed(20)
                    .withKeepSeparator(true)
                    .withMaxNumChunks(20_000)
                    .build();

            List<Document> chunks = new ArrayList<>();
            for (Document d : docs) {
                chunks.addAll(splitter.split(d));
            }

            // 3) VectorStore 저장(내부에서 임베딩 호출)
            store.add(chunks);

            return id;
        } catch (Exception e) {
            throw new RuntimeException("PDF ingest 실패: " + e.getMessage(), e);
        } finally {
            // if (tempFile != null) try { Files.deleteIfExists(temp); } catch (IOException ignore) {}
        }
    }

    private List<Document> readPdfAsDocuments(Path path, Map<String, Object> baseMeta) {
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

    /**
     * 공통 메타데이터 추가 + 텍스트 정규화
     */
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