package com.dawn.cs_study.ai.api;

import com.dawn.cs_study.ai.api.response.ApiResponse;
import com.dawn.cs_study.ai.application.RagUseCase;
import com.dawn.cs_study.ai.api.request.QueryRequest;
import com.dawn.cs_study.ai.domain.QueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class RagController {

    private final RagUseCase ragUseCase;

    @PostMapping("/rag")
    public ResponseEntity<ApiResponse<QueryResult>> queryWithRag(@RequestBody QueryRequest request) {

        QueryResult answer = ragUseCase.retrievalAugmentedGeneration(request.query(), request.maxResults());

        return ResponseEntity.ok()
                .body(
                        new ApiResponse<>(
                                answer
                        )
                );
    }

}
