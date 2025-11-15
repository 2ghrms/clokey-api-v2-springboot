package org.clokey.domain.notification.repository;

import org.clokey.notification.entity.CodiveNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodiveNotificationRepository extends JpaRepository<CodiveNotification, Long> {}
