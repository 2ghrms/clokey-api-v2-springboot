package org.clokey.domain.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            classes = NewFollowerEvent.class,
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
}
