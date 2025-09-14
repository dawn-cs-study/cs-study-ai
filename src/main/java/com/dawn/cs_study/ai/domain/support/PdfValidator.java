package com.dawn.cs_study.ai.domain.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
public class PdfValidator {


    public static void validate(MultipartFile file) {

        if (Objects.isNull(file)) {
            log.error("비어있는 파일입니다.");
            throw new IllegalArgumentException();
        }

        String fileName = file.getOriginalFilename();

        if (Objects.isNull(fileName) || fileName.isEmpty()) {
            log.error("파일 이름이 비어있습니다.");
            throw new RuntimeException();
        }

        if (!fileName.endsWith(".pdf")
                || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new RuntimeException("PDF 파일만 가능합니다.");
        }
    }

}
