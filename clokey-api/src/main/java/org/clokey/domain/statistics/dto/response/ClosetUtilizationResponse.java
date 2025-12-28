package org.clokey.domain.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ClosetUtilizationResponse(
        @Schema(description = "활용된 옷의 개수", example = "15") Long utilizedCount,
        @Schema(description = "활용되지 않은 옷의 개수", example = "10") Long unutilizedCount,
        @Schema(description = "활용된 옷 목록") List<ClosetUtilizationResponse.Payload> utilizedClothes,
        @Schema(description = "활용되지 않은 옷 목록")
                List<ClosetUtilizationResponse.Payload> unutilizedClothes) {
    public static ClosetUtilizationResponse of(
            Long utilizedCount,
            Long unutilizedCount,
            List<Payload> utilizedClothes,
            List<Payload> unutilizedClothes) {
        return new ClosetUtilizationResponse(
                utilizedCount, unutilizedCount, utilizedClothes, unutilizedClothes);
    }

    @Schema(name = "ClosetUtilizationResponsePayload", description = "옷 정보")
    public record Payload(
            @Schema(description = "옷 이미지 URL", example = "https://example.com/image.jpg")
                    String imageUrl,
            @Schema(description = "옷 이름", example = "맨투맨") String name,
            @Schema(description = "브랜드", example = "나이키") String brand) {}
}
