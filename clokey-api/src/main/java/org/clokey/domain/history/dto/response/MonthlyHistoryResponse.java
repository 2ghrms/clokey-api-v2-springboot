package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record MonthlyHistoryResponse(
        @Schema(description = "기록 목록") List<MonthlyHistoryResponse.Payload> payloads) {

    public static MonthlyHistoryResponse of(List<Payload> payloads) {
        return new MonthlyHistoryResponse(payloads);
    }

    @Schema(name = "MonthlyHistoryResponsePayload", description = "월별 기록 정보")
    public record Payload(
            @Schema(description = "기록 ID", example = "1") Long historyId,
            @Schema(description = "첫 번째 이미지 URL", example = "https://example.com/image.jpg")
                    String firstImageUrl,
            @Schema(description = "기록 날짜", example = "2025-01-01") LocalDate historyDate) {}
}
