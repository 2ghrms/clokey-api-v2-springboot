package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.history.entity.History;

public record HistoryCreateResponse(
        @Schema(description = "생성된 기록 ID", example = "1") Long historyId) {
    public static HistoryCreateResponse from(History history) {
        return new HistoryCreateResponse(history.getId());
    }
}
