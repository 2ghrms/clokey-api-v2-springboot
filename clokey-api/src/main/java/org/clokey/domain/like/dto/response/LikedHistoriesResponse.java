package org.clokey.domain.like.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "좋아요 히스토리 조회 결과 (Slice 기반)")
public record LikedHistoriesResponse(
        @Schema(description = "히스토리 미리보기 목록") List<LikedHistoryPreview> historyPreviews,
        @Schema(description = "마지막 페이지 여부", example = "false") boolean isLast) {

    @Schema(description = "히스토리 미리보기 DTO")
    public record LikedHistoryPreview(
            @Schema(description = "히스토리 ID", example = "30") Long id,
            @Schema(
                            description = "히스토리 대표 이미지 URL",
                            example =
                                    "https://clokeybucket.s3.ap-northeast-2.amazonaws.com/example.jpg")
                    String imageUrl) {}
}
