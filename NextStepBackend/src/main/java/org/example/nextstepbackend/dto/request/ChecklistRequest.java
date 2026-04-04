package org.example.nextstepbackend.dto.request;

public record ChecklistRequest(String title, Integer afterId, Integer beforeId) {}
