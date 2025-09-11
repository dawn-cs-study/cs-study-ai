package com.dawn.cs_study.ai.api;

import com.dawn.cs_study.ai.api.response.ApiResponse;
import com.dawn.cs_study.ai.application.PdfIngestService;
import com.dawn.cs_study.ai.application.RagService;
import com.dawn.cs_study.ai.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
public class RagController {

    private final PdfIngestService pdfIngestService;

    private final RagService ragService;

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentUploadResult>> uploadDocument(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "파일이 비어있습니다.")
            );
        }


        if (!file.getOriginalFilename().endsWith(".pdf")) {

            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "PDF 파일만 업로드 가능합니다.")
            );
        }

        // pdf 저장
        String id = pdfIngestService.ingest(file);

        return ResponseEntity.ok()
                .body(
                        new ApiResponse<>(true, new DocumentUploadResult(id, "문서 업로드 완료"), null)
                );


        // 관련 문서 검색
    }

    @PostMapping("/rag")
    public ResponseEntity<ApiResponse<QueryResponseDto>> queryWithRag(@RequestBody QueryRequestDto requestDto) {


        var retrieve = ragService.retrieve(requestDto.query(), requestDto.maxResults());

        String answer = ragService.generateAnswerWithContexts(requestDto.query(), retrieve);

        return ResponseEntity.ok()
                .body(
                        new ApiResponse<>(
                                true,
                                new QueryResponseDto(
                                        requestDto.query(),
                                        answer,
                                        retrieve.stream().map(DocumentSearchResult::toDocumentResponseDto).toList()),
                                null
                        )
                );
    }

}
