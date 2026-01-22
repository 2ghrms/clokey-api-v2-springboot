package org.clokey.domain.coordinate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DailyCoordinateClothResponse", description = "오늘의 코디 옷 정보")
public record DailyCoordinateClothResponse(
        @Schema(description = "옷 이미지 URL", example = "https://example.jpg") String imageUrl,
        @Schema(description = "브랜드", example = "나이키") String brand,
        @Schema(description = "옷 이름", example = "맨투맨") String name,
        @Schema(description = "하위 카테고리", example = "맨투맨") String category,
        @Schema(description = "상위 카테고리", example = "상의") String parentCategory) {
    public static DailyCoordinateClothResponse from(CoordinateDetailsListResponse details) {
        return new DailyCoordinateClothResponse(
                details.imageUrl(),
                details.brand(),
                details.name(),
                details.category(),
                details.parentCategory());
    }
}
