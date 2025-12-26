package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record StyleListResponse(
        @Schema(description = "스타일 목록") List<StyleListResponse.Content> contents) {
    @Schema(name = "StyleListResponseContent", description = "스타일 정보")
    public record Content(
            @Schema(description = "스타일 ID", example = "1") Long styleId,
            @Schema(description = "스타일 이름", example = "캐주얼") String name) {}
}
