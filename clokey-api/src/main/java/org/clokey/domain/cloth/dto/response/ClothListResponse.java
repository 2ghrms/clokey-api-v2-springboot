package org.clokey.domain.cloth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClothListResponse(
        @Schema(description = "옷 ID", example = "1") Long clothId,
        @Schema(description = "옷 이미지 url", example = "https://example.jpg") String ImageUrl,
        @Schema(description = "옷 브랜드 이름", example = "나이키") String brand,
        @Schema(description = "옷 이름", example = "파란색 후드") String name) {}
