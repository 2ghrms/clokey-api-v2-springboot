package org.clokey.domain.notification.service;

import org.clokey.domain.comment.event.NewCommentEvent;
import org.clokey.domain.comment.event.NewReplyEvent;
import org.clokey.domain.notification.dto.response.NotificationListResponse;
import org.clokey.domain.notification.dto.response.UnreadNotificationResponse;
import org.clokey.response.SliceResponse;

public interface CodiveNotificationService {

    void sendNewFollowerNotification(Long followFromId, Long followToId);

    void sendNewPendingFollowerNotification(Long followFromId, Long followToId);

    void sendNewCommentNotification(NewCommentEvent event);

    void sendNewReplyNotification(NewReplyEvent event);

    SliceResponse<NotificationListResponse> getNotificationList(
            Long lastNotificationId, Integer size);

    UnreadNotificationResponse existsUnreadNotification();

    void updateReadStatus(Long notificationId);
}
