package com.dawn.cs_study.ai.application;

import com.dawn.cs_study.ai.application.port.QueryAiPort;
import com.dawn.cs_study.ai.application.port.VectorQueryPort;
import com.dawn.cs_study.ai.domain.DocumentSearchResult;
import com.dawn.cs_study.ai.domain.QueryResult;
import com.dawn.cs_study.ai.domain.support.PromptGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagUseCase {

    private final VectorQueryPort vectorQueryPort;

    private final QueryAiPort queryAiPort;

    public QueryResult retrievalAugmentedGeneration(String question, Integer maxResults) {
        List<DocumentSearchResult> results = generateDocumentSearchResults(question, maxResults);
        return generateWithContext(question, results);
    }

    private List<DocumentSearchResult> generateDocumentSearchResults(String question, Integer maxResults) {
        List<Document> results = vectorQueryPort.similaritySearch(question, maxResults);

        return results.stream().map(
                result ->
                        new DocumentSearchResult(result.getId(), result.getText(), result.getMetadata(), result.getScore())
        ).toList();
    }

    private QueryResult generateWithContext(String question, List<DocumentSearchResult> results) {

        // 시스템 프롬프트 생성
        String systemPromptText = PromptGenerator.generateSystemPromptText(results);

        try {
            ChatResponse response = queryAiPort.ask(question, systemPromptText);

            String aiAnswer = (response != null && response.getResult() != null && response.getResult().getOutput() != null)
                    ? response.getResult().getOutput().getText()
                    : "응답을 생성할 수 없습니다.";

            // 참고 문서 정보 추가
            String sourceInfo = PromptGenerator.generateSourceInfo(results);
            return new QueryResult(question, aiAnswer + sourceInfo, results.stream().map(DocumentSearchResult::toDocumentResult).toList());
        } catch (Exception e) {
            log.error("generateWithContext Error {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
