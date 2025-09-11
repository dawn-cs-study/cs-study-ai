package com.dawn.cs_study.ai.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final OpenAiApi openAiApi;

    private final PgVectorStore pgVectorStore;

    public ChatResponse openAiChat(String userInput, String systemMessage) {

        try {
            // 메시지 구성
            List<Message> messages = List.of(
                    new SystemMessage(systemMessage),
                    new UserMessage(userInput)
            );

            // 챗 옵션 설정
            // ChatOptions - maxTokens,temperature,stopSequences .. 벤더 간 자동 변환, OpenAI stop , Anthropic stop_sequences 차이를 보정
            ChatOptions chatOptions = ChatOptions.builder()
                    .model(OpenAiApi.ChatModel.GPT_3_5_TURBO.getValue())
                    .temperature(0.7)
                    .build();

            // 프롬포트 생성
            // 모델에 보낼 메시지와, 파라미터 옵션 ChatOptions 을 감쌈
            // 보낼 메시지, 보내는 옵션 담고 있음
            Prompt prompt = new Prompt(messages, chatOptions);

            // 챗 모델 생성 및 호출
            // LLM 과 상호작용 하는 인터페이스, ChatModel 로 추상화 안하고 구현클래스 받으면 변경에 벤더 따라서 변경사항 생김
            ChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .build();
            return chatModel.call(prompt);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public void dd() {


    }


}
