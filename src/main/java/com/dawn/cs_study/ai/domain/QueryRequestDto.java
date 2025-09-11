package com.dawn.cs_study.ai.domain;

import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

public record QueryRequestDto(String query, Integer maxResults) {
}
