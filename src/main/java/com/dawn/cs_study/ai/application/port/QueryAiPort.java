package com.dawn.cs_study.ai.application.port;

import org.springframework.ai.chat.model.ChatResponse;


public interface QueryAiPort {

    ChatResponse ask(String userQuery, String systemMessage);

}
