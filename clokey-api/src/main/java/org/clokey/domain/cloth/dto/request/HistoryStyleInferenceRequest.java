package org.clokey.domain.cloth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "기록 사진 스타일 추론 요청")
public record HistoryStyleInferenceRequest(
        @NotBlank(message = "기록 이미지 URL은 비워둘 수 없습니다.")
                @Schema(description = "기록 이미지 URL", example = "https://example.com/history.jpg")
                String historyImageUrl) {}
