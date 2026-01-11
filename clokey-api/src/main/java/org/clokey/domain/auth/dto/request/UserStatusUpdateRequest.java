package org.clokey.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull(message = "활성화 여부는 필수입니다.") @Schema(description = "true: 활성화, false: 비활성화")
                Boolean active) {}
