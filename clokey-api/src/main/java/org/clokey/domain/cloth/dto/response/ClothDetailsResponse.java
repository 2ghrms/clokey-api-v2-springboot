package org.clokey.domain.cloth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.cloth.entity.Cloth;

public record ClothDetailsResponse(
        @Schema(description = "상위 카테고리 이름", example = "상의") String parentCategory,
        @Schema(description = "하위 카테고리 이름", example = "블라우스") String category,
        @Schema(description = "옷 이름", example = "스트링 리본 블라우스") String name,
        @Schema(description = "옷 브랜드", example = "로렌하이") String brand,
        @Schema(description = "구매 url", example = "https://example.com") String clothUrl) {
    public static ClothDetailsResponse from(Cloth cloth) {
        return new ClothDetailsResponse(
                cloth.getCategory().getParent().getName(),
                cloth.getCategory().getName(),
                cloth.getName(),
                cloth.getBrand(),
                cloth.getClothUrl());
    }
}
