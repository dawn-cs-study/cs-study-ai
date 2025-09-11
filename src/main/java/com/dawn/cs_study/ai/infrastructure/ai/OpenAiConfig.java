package com.dawn.cs_study.ai.infrastructure.ai;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAiApi openAiApi() {
        System.out.println(apiKey);
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .build();
    }


}
