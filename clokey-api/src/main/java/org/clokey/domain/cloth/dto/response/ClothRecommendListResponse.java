package org.clokey.domain.cloth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClothRecommendListResponse(
        @Schema(description = "옷 ID", example = "1") Long clothId,
        @Schema(description = "옷 이미지 url", example = "https://example.jpg") String ImageUrl) {}
