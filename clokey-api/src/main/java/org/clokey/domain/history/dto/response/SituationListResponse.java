package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SituationListResponse(
        @Schema(description = "상황 목록") List<SituationListResponse.Content> contents) {
    @Schema(name = "SituationListResponseContent", description = "상황 정보")
    public record Content(
            @Schema(description = "상황 ID", example = "1") Long situationId,
            @Schema(description = "상황 이름", example = "데일리") String name) {}
}
