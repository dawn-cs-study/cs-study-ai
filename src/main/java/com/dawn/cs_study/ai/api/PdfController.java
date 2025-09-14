package com.dawn.cs_study.ai.api;

import com.dawn.cs_study.ai.api.response.ApiResponse;
import com.dawn.cs_study.ai.application.EmbedPdfUseCase;
import com.dawn.cs_study.ai.domain.DocumentUploadResult;
import com.dawn.cs_study.ai.domain.support.PdfValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


@RestController
@RequiredArgsConstructor
public class PdfController {

    private final EmbedPdfUseCase embedPdfUseCase;

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentUploadResult>> uploadDocument(@RequestParam("file") MultipartFile file) {

        PdfValidator.parsePdf(file);
        String id = embedPdfUseCase.embedPdf(file);

        return ResponseEntity.ok()
                .body(
                        new ApiResponse<>(
                                new DocumentUploadResult(id, "문서 업로드 완료")
                        )
                );
    }

}
