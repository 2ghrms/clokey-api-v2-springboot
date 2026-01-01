package org.clokey.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.report.enums.TargetType;

public record ReportedCheckResponse(
        @Schema(description = "UNCHECKED 상태의 신고가 존재하는지", example = "true") boolean isReported,
        @Schema(description = "신고의 타입", example = "TargetType.HISTORY") TargetType targetType) {

    public static ReportedCheckResponse of(boolean isReported, TargetType targetType) {
        return new ReportedCheckResponse(isReported, targetType);
    }
}
