package org.clokey.domain.notification.repository;

import org.clokey.domain.notification.dto.response.NotificationListResponse;
import org.springframework.data.domain.Slice;

public interface CodiveNotificationRepositoryCustom {
    Slice<NotificationListResponse> findAllNotificationsByMemberId(
            Long memberId, Long lastNotificationId, Integer size);
}
