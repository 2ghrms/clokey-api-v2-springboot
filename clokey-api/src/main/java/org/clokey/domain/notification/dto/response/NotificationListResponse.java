package org.clokey.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.clokey.notification.enums.ReadStatus;
import org.clokey.notification.enums.RedirectType;

public record NotificationListResponse(
        @Schema(description = "알림의 ID", example = "1L") Long notificationId,
        @Schema(description = "알림의 이미지 URL", example = "https://testiamge.com")
                String notificationImageUrl,
        @Schema(description = "알림의 내용", example = "테스트 알림입니다.") String notificationContent,
        @Schema(description = "redirectInfo(historyId 등)", example = "1L") String redirectInfo,
        @Schema(description = "RedirectType(Member, History 등)", example = "MEMBER_REDIRECT")
                RedirectType redirectType,
        @Schema(description = "읽음 상태(ReadStatus)", example = "NOT_READ") ReadStatus readStatus,
        @Schema(description = "알림이 생성된 시간", example = "2025-11-25T12:39:03.123456")
                LocalDateTime createdAt) {}
