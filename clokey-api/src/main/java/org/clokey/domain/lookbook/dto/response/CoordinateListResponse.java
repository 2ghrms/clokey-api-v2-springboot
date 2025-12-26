package org.clokey.domain.lookbook.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CoordinateListResponse(
        @Schema(description = "코디 ID", example = "1") Long coordinateId,
        @Schema(description = "코디 이름", example = "데이트 코디") String coordinateName,
        @Schema(description = "코디 좋아요 여부", example = "true") Boolean coordinateLiked,
        @Schema(description = "코디 사진", example = "https://example.jpg") String imageUrl) {}
