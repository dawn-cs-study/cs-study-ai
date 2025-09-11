package com.dawn.cs_study.ai.application;

import com.dawn.cs_study.ai.domain.DocumentSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final PgVectorStore pgVectorStore;

    private final ChatService chatService;

    public List<DocumentSearchResult> retrieve(String question, Integer maxResults) {
        return similaritySearch(question, maxResults);
    }

    public String generateAnswerWithContexts(String question, List<DocumentSearchResult> relevantDocs) {

        List<String> numberedDocs =
                java.util.stream.IntStream.range(0, relevantDocs.size())
                        .mapToObj(i -> "[" + (i + 1) + "] " + relevantDocs.get(i).content())
                        .toList();

        String context = String.join("\n\n", numberedDocs);

        // 시스템 프롬프트 생성
        String systemPromptText = """
                당신은 지식 기반 Q&A 시스템입니다.
                사용자의 질문에 대한 답변을 다음 정보를 바탕으로 생성해주세요.
                주어진 정보에 답이 없다면 모른다고 솔직히 말해주세요.
                답변 마지막에 사용한 정보의 출처 번호 [1], [2] 등을 반드시 포함해주세요.
                            
                정보:
                %s
                """.formatted(context);

        try {
            ChatResponse response = chatService.openAiChat(question, systemPromptText);

            String aiAnswer = (response != null && response.getResult() != null && response.getResult().getOutput() != null)
                    ? response.getResult().getOutput().getText()
                    : "응답을 생성할 수 없습니다.";

            // 참고 문서 정보 추가
            StringBuilder sourceInfo = new StringBuilder();
            sourceInfo.append("\n\n참고 문서:\n");
            for (int i = 0; i < relevantDocs.size(); i++) {
                DocumentSearchResult doc = relevantDocs.get(i);
                String originalFilename = doc.metadata().getOrDefault("originalFilename", "Unknown file").toString();
                sourceInfo.append("[").append(i + 1).append("] ").append(originalFilename).append("\n");
            }

            return aiAnswer + sourceInfo;

        } catch (Exception e) {
            return "AI 모델 호출 중 오류가 발생했습니다. 검색 결과만 제공합니다:\n\n" +
                    relevantDocs.stream()
                            .map(DocumentSearchResult::content)
                            .collect(Collectors.joining("\n\n"));
        }


    }

    private List<DocumentSearchResult> similaritySearch(String question, Integer maxResults) {


        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(question)
                    .topK(maxResults)
                    .build();

            List<Document> results = pgVectorStore.similaritySearch(searchRequest);

            return results.stream().map(
                    result ->
                            new DocumentSearchResult(
                                    result.getId(),
                                    result.getText(),
                                    result.getMetadata(),
                                    result.getScore()
                            )
            ).toList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
