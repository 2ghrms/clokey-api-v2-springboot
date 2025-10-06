package org.clokey.domain.coordinate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.coordinate.entity.Coordinate;

public record CoordinateCreateResponse(
        @Schema(description = "생성된 코디 ID", example = "1") Long coordinateId) {
    public static CoordinateCreateResponse from(Coordinate coordinate) {
        return new CoordinateCreateResponse(coordinate.getId());
    }
}
