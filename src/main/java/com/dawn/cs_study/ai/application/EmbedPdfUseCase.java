package com.dawn.cs_study.ai.application;// PdfIngestService.java

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.dawn.cs_study.ai.application.port.ReadPdfPort;
import com.dawn.cs_study.ai.application.port.VectorCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbedPdfUseCase {

    private final VectorCommandPort vectorCommandPort;

    private final ReadPdfPort readPdfPort;

    /**
     * PDF 업로드를 받아 pgvector에 저장
     */
    // todo 개인 경로를, meta data 로 저장, 파일 저장 로직 고민하기
    public String embedPdf(MultipartFile file) {
        String id = UUID.randomUUID().toString();

        File tempFile = null;
        Path temp = null;

        try {
            // 0) 임시 파일 저장

            String original = Objects.requireNonNullElse(file.getOriginalFilename(), "upload.pdf");

            String uploadDir = "/Users/seungjae/Desktop/fs";

            temp = Files.createTempFile(Path.of(uploadDir), "upload_", original);
            tempFile = temp.toFile();

            // 저장
            file.transferTo(tempFile);

            // 1) PDF → Document (문단→페이지 폴백)
            Map<String, Object> baseMeta = Map.of(
                    "id", id,
                    "filename", original
            );
            List<Document> docs = readPdfPort.readPdfAsDocuments(temp, baseMeta);

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

            // 3) VectorStore 저장
            vectorCommandPort.add(chunks);

            return id;
        } catch (Exception e) {
            throw new RuntimeException("PDF ingest 실패: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) try {
                Files.deleteIfExists(temp);
            } catch (IOException ignore) {
                log.error("temp file delete error: {}", tempFile.getAbsolutePath());
            }
        }
    }

}