package com.dawn.cs_study.ai.api;

import com.dawn.cs_study.ai.api.request.ChatRequest;
import com.dawn.cs_study.ai.api.response.ApiResponse;
import com.dawn.cs_study.ai.application.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    @PostMapping("/query")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendMessage(@RequestBody ChatRequest request) {

        String systemMessage = "You are a helpful AI assistant.";

        ChatResponse response = chatService.openAiChat(request.query(), systemMessage);

        return ResponseEntity.ok()
                .body(new ApiResponse<>(
                        true,
                        Map.of("answer", response.getResult().getOutput().getText()),
                        null
                ));

    }

}
