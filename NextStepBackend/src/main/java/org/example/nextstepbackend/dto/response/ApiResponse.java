package org.example.nextstepbackend.dto.response;

public record ApiResponse<T>(
        ResponseMetaData metaData,
        T data
) { }
