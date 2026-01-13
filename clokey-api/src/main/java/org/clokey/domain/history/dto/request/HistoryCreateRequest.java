package org.clokey.domain.history.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Schema(description = "기록 생성 요청")
public record HistoryCreateRequest(
        @Size(max = 120, message = "기록의 내용은 최대 120자까지 가능합니다.")
                @Schema(
                        description = "기록의 내용",
                        example = "안녕 오늘 오지게 덥다 ㄷㄷ;; 근데 한달 뒤면 가을임 벌써 가을 기대 만발ㅋ")
                String content,
        @NotNull(message = "상황 ID는 비워둘 수 없습니다.") @Schema(description = "상황 ID", example = "7")
                Long situationId,
        @NotNull(message = "스타일 목록은 비워둘 수 없습니다.")
                @Size(min = 1, max = 3, message = "스타일은 1~3개만 선택 가능합니다.")
                @Schema(description = "스타일 ID 목록", example = "[1, 3]")
                List<@NotNull Long> styleIds,
        @ArraySchema(
                        schema = @Schema(description = "해시태그 문자열", example = "#ootd"),
                        arraySchema =
                                @Schema(description = "해시태그 문자열 목록 (예: [\"#ootd\",\"#봄코디\"])"))
                List<@NotBlank(message = "해시태그는 비워둘 수 없습니다.") String> hashtags,
        @NotNull(message = "기록 이미지 목록은 비워둘 수 없습니다.")
                @Schema(description = "기록 이미지 목록")
                @Size(min = 1, max = 10, message = "이미지는 1~10개만 첨부할 수 있습니다.")
                List<@Valid Payload> payloads) {
    @Schema(description = "이미지 위 특정 옷의 위치 태그(정규화 좌표 권장)")
    public record ClothTag(
            @NotNull(message = "옷 ID는 비워둘 수 없습니다.") @Schema(description = "옷 ID", example = "123")
                    Long clothId,
            @NotNull(message = "옷의 x좌표는 비워둘 수 없습니다.")
                    @Positive(message = "옷의 x좌표는 음수일 수 없습니다.")
                    @Schema(description = "옷의 X 좌표", example = "0.42")
                    Double locationX,
            @NotNull(message = "옷의 y좌표는 비워둘 수 없습니다.")
                    @Positive(message = "옷의 y좌표는 음수일 수 없습니다.")
                    @Schema(description = "옷의 Y 좌표", example = "0.73")
                    Double locationY) {}

    @Schema(name = "HistoryCreatePayload", description = "기록 이미지 1건")
    public record Payload(
            @NotBlank(message = "기록의 사진은 비워둘 수 없습니다.")
                    @Schema(description = "기록의 사진 URL", example = "https://.../image.jpg")
                    String imageUrl,
            @Schema(description = "옷 태그 목록") List<@Valid ClothTag> clothTags) {}
}
