package com.dawn.cs_study.ai.api;

import com.dawn.cs_study.ai.api.response.ApiResponse;
import com.dawn.cs_study.ai.application.EmbedPdfDisUseCase;
import com.dawn.cs_study.ai.application.EmbedPdfUseCase;
import com.dawn.cs_study.ai.domain.DocumentUploadResult;
import com.dawn.cs_study.ai.domain.support.PdfValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PdfController {

    private final EmbedPdfUseCase embedPdfUseCase;

    private final EmbedPdfDisUseCase embedPdfDisUseCase;

    @GetMapping("/happy")
    public String gg() {
        return "good";
    }


    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentUploadResult>> uploadDocument(@RequestParam("file") MultipartFile file) {

         String id = embedPdfUseCase.embedPdf(file);

        return ResponseEntity.ok()
                .body(
                        new ApiResponse<>(
                                new DocumentUploadResult(id, "문서 업로드 완료")
                        )
                );
    }

    // todo 성능 비교 및 최적화 과정 진행 중

    @PostMapping(value = "/documents2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentUploadResult>> aa(@RequestParam("file") MultipartFile file) {
        String id = embedPdfDisUseCase.embedPdf(file);

        return ResponseEntity.ok()
                .body(
                        new ApiResponse<>(
                                new DocumentUploadResult(id, "문서 업로드 완료")
                        )
                );
    }

}
