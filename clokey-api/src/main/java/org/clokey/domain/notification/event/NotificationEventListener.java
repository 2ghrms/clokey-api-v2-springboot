package org.clokey.domain.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.domain.comment.event.NewCommentEvent;
import org.clokey.domain.comment.event.NewReplyEvent;
import org.clokey.domain.like.event.NewLikeEvent;
import org.clokey.domain.member.event.NewFollowerEvent;
import org.clokey.domain.member.event.NewPendingFollowerEvent;
import org.clokey.domain.notification.service.CodiveNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final CodiveNotificationService codiveNotificationService;

    @Async
    @TransactionalEventListener(
            classes = NewFollowerEvent.class,
            phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewFollower(NewFollowerEvent event) {
        try {
            codiveNotificationService.sendNewFollowerNotification(
                    event.followFromId(), event.followToId());
        } catch (Exception e) {
            log.error(
                    "새 팔로워 알림 전송 실패 - from: {}, to: {}",
                    event.followFromId(),
                    event.followToId(),
                    e);
        }
    }

    @Async
    @TransactionalEventListener(
            classes = NewPendingFollowerEvent.class,
            phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewPendingFollower(NewPendingFollowerEvent event) {
        try {
            codiveNotificationService.sendNewPendingFollowerNotification(
                    event.followFromId(), event.followToId());
        } catch (Exception e) {
            log.error(
                    "새 팔로워 요청 알림 전송 실패 - from: {}, to: {}",
                    event.followFromId(),
                    event.followToId(),
                    e);
        }
    }

    @Async
    @TransactionalEventListener(
            classes = NewCommentEvent.class,
            phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewComment(NewCommentEvent event) {
        try {
            codiveNotificationService.sendNewCommentNotification(event);
        } catch (Exception e) {
            log.error(
                    "새 댓글 알림 전송 실패 - historyId: {}, commentId: {}",
                    event.historyId(),
                    event.commentId(),
                    e);
        }
    }

    @Async
    @TransactionalEventListener(
            classes = NewReplyEvent.class,
            phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewReply(NewReplyEvent event) {
        try {
            codiveNotificationService.sendNewReplyNotification(event);
        } catch (Exception e) {
            log.error(
                    "새 대댓글 알림 전송 실패 - historyId: {}, replyId: {}",
                    event.historyId(),
                    event.replyId(),
                    e);
        }
    }

    @Async
    @TransactionalEventListener(classes = NewLikeEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewLike(NewLikeEvent event) {
        try {
            codiveNotificationService.sendNewLikeNotification(event);
        } catch (Exception e) {
            log.error(
                    "새 좋아요 알림 전송 실패 - historyId: {}, likeId: {}",
                    event.historyId(),
                    event.likeId(),
                    e);
        }
    }
}
