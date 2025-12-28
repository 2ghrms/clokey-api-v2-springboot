package org.clokey.domain.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FavoriteCategoryItemsResponse(
        @Schema(description = "카테고리별 최애 아이템 목록")
                List<FavoriteCategoryItemsResponse.Payload> payloads) {
    public static FavoriteCategoryItemsResponse of(List<Payload> payloads) {
        return new FavoriteCategoryItemsResponse(payloads);
    }

    @Schema(name = "FavoriteCategoryItemsResponsePayload", description = "카테고리별 최애 아이템 정보")
    public record Payload(
            @Schema(description = "2차 카테고리 ID (기타의 경우 null)", example = "10") Long categoryId,
            @Schema(description = "2차 카테고리 이름", example = "맨투맨") String categoryName,
            @Schema(description = "점유율", example = "0.35") Double occupancyRate,
            @Schema(description = "옷의 개수", example = "15") Long clothCount) {}
}
