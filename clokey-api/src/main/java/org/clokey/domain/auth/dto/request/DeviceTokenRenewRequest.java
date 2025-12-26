package org.clokey.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRenewRequest(
        @NotBlank(message = "Device Token은 비워둘 수 없습니다.")
                @Schema(description = "기기를 식별할 수 있는 Device Token")
                String deviceToken) {}
