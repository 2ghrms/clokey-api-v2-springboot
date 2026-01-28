package org.clokey.domain.cloth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.clokey.cloth.enums.Season;

@Schema(description = "옷 정보 추출 응답")
public record ClothInfoExtractResponse(@Schema(description = "옷 정보 목록") List<Payload> payloads) {

    public static ClothInfoExtractResponse of(List<Payload> payloads) {
        return new ClothInfoExtractResponse(payloads);
    }

    @Schema(name = "ClothInfoExtractResponsePayload", description = "옷 정보")
    public record Payload(
            @Schema(description = "새로운 옷 사진 URL", example = "https://example.com/cloth.jpg")
                    String clothImageUrl,
            @Schema(description = "계절 목록", example = "SPRING") List<Season> seasons,
            @Schema(description = "상위 카테고리 ID", example = "1") Long parentCategoryId,
            @Schema(description = "상위 카테고리 이름", example = "상의") String parentCategoryName,
            @Schema(description = "카테고리 ID", example = "11") Long categoryId,
            @Schema(description = "카테고리 이름", example = "후드티") String categoryName) {}
}
