package org.clokey.domain.coordinate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record DailyCoordinateCreateRequest(
        @NotBlank(message = "오늘의 코디의 사진은 비워둘 수 없습니다.")
                @Schema(description = "오늘의 코디의 사진", example = "https://example.jpg")
                String coordinateImageUrl,
        @NotEmpty(message = "옷들의 정보를 비워둘 수 없습니다.") @Valid @Schema(description = "코디에 등록할 옷들")
                List<Payload> payloads) {
    @Schema(name = "DailyCoordinateCreateRequestPayload", description = "오늘의 코디 생성 요청 DTO")
    public record Payload(
            @NotNull(message = "옷 ID는 비워둘 수 없습니다.")
                    @Schema(description = "오늘의 코디에 등록되는 옷ID", example = "1")
                    Long clothId,
            @NotNull(message = "옷의 x좌표는 비워둘 수 없습니다.")
                    @Schema(description = "오늘의 코디에 등록되는 옷의 X좌표")
                    @Positive(message = "옷의 x좌표는 음수일 수 없습니다.")
                    Double locationX,
            @NotNull(message = "옷의 y좌표는 비워둘 수 없습니다.")
                    @Schema(description = "오늘의 코디에 등록되는 옷의 Y좌표")
                    @Positive(message = "옷의 y좌표는 음수일 수 없습니다.")
                    Double locationY,
            @NotNull(message = "옷의 비율은 비워둘 수 없습니다.")
                    @Positive(message = "옷의 비율은 음수일 수 없습니다.")
                    @Schema(description = "오늘의 코디 옷의 크기 비율")
                    Double ratio,
            @NotNull(message = "옷의 각도는 비워둘 수 없습니다.")
                    @Min(value = 0, message = "각도는 0도 이상이어야 합니다.")
                    @Max(value = 360, message = "각도는 360도 이하여야 합니다.")
                    @Schema(description = "오늘의 코디 옷의 각도")
                    Double degree,
            @NotNull(message = "옷의 순서는 비워둘 수 없습니다.") @Schema(description = "오늘의 코디 옷의 순서")
                    Integer order) {}
}
