package com.dawn.cs_study.ai.application;

import com.dawn.cs_study.ai.application.port.VectorCommandPort;
import com.dawn.cs_study.ai.domain.support.PdfValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbedPdfDisUseCase {

    private final VectorCommandPort vectorCommandPort;

    public String embedPdf(MultipartFile file) {
        final String id = UUID.randomUUID().toString();
        final String original = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.pdf");

        PdfValidator.validate(file);

        Path tempPath = null;
        try {
            // 1) OS 임시 디렉토리에 파일 저장
            tempPath = Files.createTempFile("/Users/seungjae/Desktop/tt", "_" + original);
            File tempFile = tempPath.toFile();
            file.transferTo(tempFile);

            // 2) PDF → Document + Split
            List<Document> chunks = convertToDocuments(tempFile, id, original);

            // 3) VectorStore 저장
            vectorCommandPort.embedDocuments(chunks);
            log.info("Embedded {} chunks for PDF [{}]", chunks.size(), original);

            return id;
        } catch (Exception e) {
            log.error("PDF embed 실패 filename={}", original, e);
            throw new RuntimeException("PDF ingest 실패: " + e.getMessage(), e);
        } finally {
            if (tempPath != null) {
                try {
                    Files.deleteIfExists(tempPath);
                    log.debug("임시 파일 삭제 완료: {}", tempPath);
                } catch (Exception ex) {
                    log.warn("임시 파일 삭제 실패: {}", tempPath, ex);
                }
            }
        }
    }

    private List<Document> convertToDocuments(File tempFile, String id, String original) {
        Map<String, Object> baseMeta = Map.of("id", id, "filename", original);

        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(1)
                .build();

        PagePdfDocumentReader reader = new PagePdfDocumentReader(new FileSystemResource(tempFile), config);

        List<Document> docs = reader.read();
        for (Document d : docs) {
            d.getMetadata().putAll(baseMeta);
        }

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(900)
                .withMinChunkSizeChars(400)
                .withMinChunkLengthToEmbed(20)
                .withKeepSeparator(true)
                .withMaxNumChunks(20_000)
                .build();

        return splitter.apply(docs);
    }
}