package org.clokey.domain.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FavoriteItemsResponse(
        @Schema(description = "카테고리별 최애 아이템 목록") List<FavoriteItemsResponse.Payload> payloads) {
    public static FavoriteItemsResponse of(List<Payload> payloads) {
        return new FavoriteItemsResponse(payloads);
    }

    @Schema(name = "FavoriteItemsResponsePayload", description = "카테고리별 최애 아이템 정보")
    public record Payload(
            @Schema(description = "2차 카테고리 ID", example = "10") Long categoryId,
            @Schema(description = "2차 카테고리 이름", example = "맨투맨") String categoryName,
            @Schema(description = "옷의 개수", example = "15") Long clothCount) {}
}
