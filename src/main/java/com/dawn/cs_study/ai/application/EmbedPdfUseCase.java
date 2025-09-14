package com.dawn.cs_study.ai.application;

import com.dawn.cs_study.ai.application.port.VectorCommandPort;
import com.dawn.cs_study.ai.domain.support.PdfValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbedPdfUseCase {

    private final VectorCommandPort vectorCommandPort;

    /**
     * PDF 업로드를 받아 원본 저장 없이 pgvector에 저장
     */
    public String embedPdf(MultipartFile file) {

        // PdfValidator.validate(file);

        final String id = UUID.randomUUID().toString();
        final String original = file.getOriginalFilename();

        ByteArrayResource resource = convertToByteArrayResource(file, original);
        List<Document> chunks = convertToDocuments(resource, id, original);
        vectorCommandPort.embedDocuments(chunks);
        return id;
    }

    private ByteArrayResource convertToByteArrayResource(MultipartFile file, String original) {

        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return original;
                }
            };
        } catch (Exception e) {
            log.error("ByteArray 변환 실패 {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private List<Document> convertToDocuments(ByteArrayResource resource, String id, String original) {


        // PDF → Document (페이지 단위)
        Map<String, Object> baseMeta = Map.of("id", id, "filename", original);
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(1)
                .build();

        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource, config);

        List<Document> docs = reader.read();
        for (Document d : docs) {
            d.getMetadata().putAll(baseMeta);
        }

        // 토큰 단위 Split
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