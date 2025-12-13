package org.example.nextstepbackend.Utils;

import org.example.nextstepbackend.Dto.Response.ApiResponse;
import org.example.nextstepbackend.Dto.Response.ResponseMetaData;

import java.time.LocalDateTime;
import java.util.List;

public class ApiResponseUtil {

    // Success with code + message
    public static <T>ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(
                new ResponseMetaData(
                        true,
                        code,
                        message,
                        LocalDateTime.now(),
                        List.of()
                ),
                data
        );
    }

    // Error 1 message with code
    public static ApiResponse<?> error(String code, String message) {
        return new ApiResponse<>(
                new ResponseMetaData(
                        false,
                        code,
                        message,
                        LocalDateTime.now(),
                        List.of(message)
                ),
                null
        );
    }

    // Error multi message with code
    public static ApiResponse<?> error(String code, String message, List<String> errors) {
        return new ApiResponse<>(
                new ResponseMetaData(
                        false,
                        code,
                        message,
                        LocalDateTime.now(),
                        errors
                ),
                null
        );
    }

    // Validation error list
    public static ApiResponse<?> validationError(List<String> errors) {
        return new ApiResponse<>(
                new ResponseMetaData(
                        false,
                        "VALIDATION_ERROR",
                        "Validation failed",
                        LocalDateTime.now(),
                        errors
                ),
                null
        );
    }
}
