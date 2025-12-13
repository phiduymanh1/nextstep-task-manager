package org.example.nextstepbackend.Dto.Response;

public record ApiResponse<T>(
        ResponseMetaData metaData,
        T data
) { }
