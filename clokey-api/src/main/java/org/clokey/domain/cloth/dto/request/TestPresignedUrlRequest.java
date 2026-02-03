package org.clokey.domain.cloth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "테스트용 presignedUrl 발급 요청 (로컬 환경 전용)")
public record TestPresignedUrlRequest(
        @NotNull(message = "발급할 presignedUrl 개수는 필수입니다.")
                @Min(value = 1, message = "발급할 presignedUrl 개수는 1개 이상이어야 합니다.")
                @Schema(description = "발급할 presignedUrl 개수", example = "5")
                Integer count) {}
