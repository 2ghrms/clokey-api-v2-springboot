package org.clokey.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UnreadNotificationResponse(
        @Schema(description = "안읽음 알림의 존재 유무", example = "true")
                boolean existsUnreadNotification) {}
