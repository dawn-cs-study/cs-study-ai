package com.dawn.cs_study.ai.api.response;

public record ApiResponse<T>(Boolean success, T data, String error) {

    public ApiResponse(T data) {
        this(true, data, null);
    }

    public ApiResponse(String error) {
        this(false, null, error);
    }

}
