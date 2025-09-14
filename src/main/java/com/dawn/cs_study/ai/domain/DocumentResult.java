package com.dawn.cs_study.ai.domain;

import java.util.Map;

public record DocumentResult(String id, Double score, String content, Map<String, Object> metadata) {

}


