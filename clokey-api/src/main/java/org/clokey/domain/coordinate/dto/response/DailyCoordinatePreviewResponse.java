package org.clokey.domain.coordinate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import org.clokey.coordinate.entity.Coordinate;

public record DailyCoordinatePreviewResponse(
        @Schema(description = "오늘의 코디의 ID", example = "1") Long coordinateId,
        @Schema(description = "오늘의 코디의 imageUrl", example = "https://example.jpg") String imageUrl,
        @Schema(description = "오늘의 코디의 날짜", example = "2026-02-04") LocalDate date) {
    public static DailyCoordinatePreviewResponse from(Coordinate coordinate) {
        return new DailyCoordinatePreviewResponse(
                coordinate.getId(),
                coordinate.getImageUrl(),
                coordinate.getUpdatedAt().toLocalDate());
    }
}
