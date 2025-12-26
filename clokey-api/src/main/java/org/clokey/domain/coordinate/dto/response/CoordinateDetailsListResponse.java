package org.clokey.domain.coordinate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CoordinateDetailsListResponse(
        @Schema(description = "코디-옷 ID", example = "1") Long coordinateClothId,
        @Schema(description = "코디-옷의 X좌표", example = "50.6") Double locationX,
        @Schema(description = "코디-옷의 Y좌표", example = "120.6") Double locationY,
        @Schema(description = "코디-옷의 비율", example = "1.5") Double ratio,
        @Schema(description = "코디-옷의 각도", example = "240.6") Double degree,
        @Schema(description = "코디-옷의 순서", example = "1") int order,
        @Schema(description = "옷의 imageUrl", example = "https://example.jpg") String imageUrl,
        @Schema(description = "옷 브랜드", example = "나이키") String brand,
        @Schema(description = "옷 이름", example = "나이키 맨투맨") String name,
        @Schema(description = "옷의 카테고리", example = "맨투맨") String category,
        @Schema(description = "옷의 상위 카테고리", example = "상의") String parentCategory) {}
