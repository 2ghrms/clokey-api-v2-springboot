package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TodayHistoryExistenceResponse(
        @Schema(description = "오늘 기록 존재 여부", example = "true") boolean exists) {
    public static TodayHistoryExistenceResponse of(boolean exists) {
        return new TodayHistoryExistenceResponse(exists);
    }
}
