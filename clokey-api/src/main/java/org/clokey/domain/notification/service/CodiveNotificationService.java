package org.clokey.domain.notification.service;

public interface CodiveNotificationService {

    void sendNewFollowerNotification(Long followFromId, Long followToId);

    void sendNewPendingFollowerNotification(Long followFromId, Long followToId);
}
