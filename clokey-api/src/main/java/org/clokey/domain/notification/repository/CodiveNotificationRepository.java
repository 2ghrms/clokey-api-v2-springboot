package org.clokey.domain.notification.repository;

import org.clokey.member.entity.Member;
import org.clokey.notification.entity.CodiveNotification;
import org.clokey.notification.enums.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodiveNotificationRepository
        extends JpaRepository<CodiveNotification, Long>, CodiveNotificationRepositoryCustom {

    boolean existsByMemberAndReadStatus(Member currentMember, ReadStatus readStatus);
}
