package org.example.nextstepbackend.dto.response.Workspace;

import org.example.nextstepbackend.dto.response.board.BoardResponse;
import org.example.nextstepbackend.dto.response.common.PageResponse;

public record WorkspaceDetailResponse(
    Integer id, String name, String slug, PageResponse<BoardResponse> boards) {}
