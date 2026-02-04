package org.clokey.domain.coordinate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.coordinate.entity.Coordinate;

public record FavoriteCoordinateResponse(
        @Schema(description = "코디 ID", example = "1") Long coordinateId,
        @Schema(description = "코디 이미지 url", example = "https://exampe.com") String imageUrl,
        @Schema(description = "코디 이름", example = "영화관 데이트") String coordinateName) {
    public static FavoriteCoordinateResponse from(Coordinate coordinates) {
        return new FavoriteCoordinateResponse(
                coordinates.getId(), coordinates.getImageUrl(), coordinates.getName());
    }
}
