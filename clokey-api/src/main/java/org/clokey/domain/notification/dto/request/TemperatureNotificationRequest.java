package org.clokey.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TemperatureNotificationRequest(
        @NotNull(message = "온도는 비워둘 수 없습니다.") @Schema(description = "현재 기온", example = "-10.5")
                Double temperature) {}
