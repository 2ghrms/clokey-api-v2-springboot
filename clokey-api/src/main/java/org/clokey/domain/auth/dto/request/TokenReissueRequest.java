package org.clokey.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(
        @NotBlank(message = "리프레시 토큰은 비워둘 수 없습니다.") @Schema(description = "토큰 재발급을 위한 리프레시 토큰")
                String refreshToken) {}
