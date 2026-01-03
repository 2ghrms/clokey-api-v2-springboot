package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record HistoryClothTagListResponse(
        @Schema(description = "옷 태그 목록") List<HistoryClothTagListResponse.Payload> payloads) {

    public static HistoryClothTagListResponse of(List<Payload> payloads) {
        return new HistoryClothTagListResponse(payloads);
    }

    @Schema(name = "HistoryClothTagListResponsePayload", description = "옷 태그 정보")
    public record Payload(
            @Schema(description = "옷 태그 ID", example = "1") Long historyClothTagId,
            @Schema(description = "옷 ID", example = "1") Long clothId,
            @Schema(description = "옷 이미지 URL", example = "https://example.com/image.jpg")
                    String clothImageUrl,
            @Schema(description = "옷 이름", example = "맨투맨") String name,
            @Schema(description = "옷 브랜드", example = "나이키") String brand,
            @Schema(description = "X 좌표", example = "0.5") Double locationX,
            @Schema(description = "Y 좌표", example = "0.7") Double locationY) {}
}
