package org.clokey.domain.lookbook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LookBookUpdateRequest(
        @NotBlank(message = "룩북의 이름은 비워둘 수 없습니다.")
                @Schema(description = "룩북의 이름", example = "데이트 룩북")
                String name) {}
