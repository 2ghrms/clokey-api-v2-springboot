package org.clokey.domain.coordinate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.coordinate.entity.Coordinate;

public record FavoriteCoordinateResponse(
        @Schema(description = "코디 ID") Long coordinateId,
        @Schema(description = "코디 이미지 url") String imageUrl) {
    public static FavoriteCoordinateResponse from(Coordinate coordinates) {
        return new FavoriteCoordinateResponse(coordinates.getId(), coordinates.getImageUrl());
    }
}
