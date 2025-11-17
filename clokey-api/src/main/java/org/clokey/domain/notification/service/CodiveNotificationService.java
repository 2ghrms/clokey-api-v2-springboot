package org.clokey.domain.notification.service;

import org.clokey.domain.comment.event.NewCommentEvent;
import org.clokey.domain.comment.event.NewReplyEvent;

public interface CodiveNotificationService {

    void sendNewFollowerNotification(Long followFromId, Long followToId);

    void sendNewPendingFollowerNotification(Long followFromId, Long followToId);

    void sendNewCommentNotification(NewCommentEvent event);

    void sendNewReplyNotification(NewReplyEvent event);
}
