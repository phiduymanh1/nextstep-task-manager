package org.example.nextstepbackend.dto.response.workspace;

import org.example.nextstepbackend.dto.response.board.BoardResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;
import org.example.nextstepbackend.enums.Visibility;

public record WorkspaceDetailResponse(
    Integer id,
    String name,
    String slug,
    Visibility visibility,
    PageResponse<BoardResponse> boards) {}
