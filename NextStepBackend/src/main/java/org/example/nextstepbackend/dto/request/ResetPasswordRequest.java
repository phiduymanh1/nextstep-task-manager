package org.example.nextstepbackend.dto.request;

public record ResetPasswordRequest(String token, String newPassword) {}
