package org.clokey.domain.cloth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "기록 사진 스타일 추론 응답")
public record HistoryStyleInferenceResponse(
        @Schema(description = "상황 ID", example = "1") Long situationId,
        @Schema(description = "상황 이름", example = "데일리") String situationName,
        @Schema(description = "스타일 목록") List<StylePayload> styles) {

    public static HistoryStyleInferenceResponse of(
            Long situationId, String situationName, List<StylePayload> styles) {
        return new HistoryStyleInferenceResponse(situationId, situationName, styles);
    }

    @Schema(name = "HistoryStyleInferenceResponseStylePayload", description = "스타일 정보")
    public record StylePayload(
            @Schema(description = "스타일 ID", example = "1") Long styleId,
            @Schema(description = "스타일 이름", example = "캐주얼") String styleName) {}
}
