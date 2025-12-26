package org.clokey.domain.coordinate.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CoordinateAutoCreateRequest(
        @NotBlank(message = "코디의 이름은 비워둘 수 없습니다.")
                @Schema(description = "코디의 이름", example = "데이트 코디")
                String name,
        @Size(max = 100, message = "메모는 최대 100자까지 입력할 수 있습니다.")
                @Schema(description = "코디 메모", example = "내일 이거 입고 나가야 함")
                String memo,
        @NotNull(message = "일일 코디 ID는 비워둘 수 없습니다.") @Schema(description = "일일 코디 ID", example = "1")
                Long dailyCoordinateId,
        @NotNull(message = "룩북 ID는 비워둘 수 없습니다.") @Schema(description = "룩북 ID", example = "1")
                Long lookBookId) {}
