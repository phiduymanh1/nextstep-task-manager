package org.example.nextstepbackend.dto.response.common;

public record ApiResponse<T>(ResponseMetaData metaData, T data) {}
