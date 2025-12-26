package org.clokey.domain.coordinate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.coordinate.entity.Coordinate;

public record CoordinatePreviewResponse(
        @Schema(description = "코디의 ID", example = "1") Long coordinateId,
        @Schema(description = "코디의 imageUrl", example = "https://example.jpg") String imageUrl,
        @Schema(description = "코디의 이름", example = "영화관 데이트") String coordinateName,
        @Schema(description = "코디의 메모", example = "담주에 꼭 입고 가기") String coordinateMemo) {
    public static CoordinatePreviewResponse from(Coordinate coordinate) {
        return new CoordinatePreviewResponse(
                coordinate.getId(),
                coordinate.getImageUrl(),
                coordinate.getName(),
                coordinate.getMemo());
    }
}
