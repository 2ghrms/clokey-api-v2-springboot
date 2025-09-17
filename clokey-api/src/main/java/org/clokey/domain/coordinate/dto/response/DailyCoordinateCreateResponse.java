package org.clokey.domain.coordinate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.coordinate.entity.Coordinate;

public record DailyCoordinateCreateResponse(
        @Schema(description = "생성된 오늘의 코디 ID", example = "1") Long dailyCoordinateId) {
    public static DailyCoordinateCreateResponse from(Coordinate coordinate) {
        return new DailyCoordinateCreateResponse(coordinate.getId());
    }
}
