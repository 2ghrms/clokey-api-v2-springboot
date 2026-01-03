package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record HistoryOwnershipCheckResponse(
        @Schema(description = "내 기록 여부", example = "true") boolean isOwner) {
    public static HistoryOwnershipCheckResponse of(boolean isOwner) {
        return new HistoryOwnershipCheckResponse(isOwner);
    }
}
