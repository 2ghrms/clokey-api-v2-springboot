package org.clokey.domain.lookbook.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LookBookListResponse(
        @Schema(description = "룩북 ID", example = "1") Long lookBookId,
        @Schema(description = "룩북 이름", example = "데이트 룩") String lookBookName,
        @Schema(description = "룩북 개수", example = "3") Long count,
        @Schema(description = "대표 코디 사진 (첫 번째 코디)", example = "https://example.jpg")
                String imageUrl) {}
