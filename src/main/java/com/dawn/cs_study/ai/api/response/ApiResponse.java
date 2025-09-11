package com.dawn.cs_study.ai.api.response;

public record ApiResponse<T>(Boolean success, T data, String error) {
}
