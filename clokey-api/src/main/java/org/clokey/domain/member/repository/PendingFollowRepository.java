package org.clokey.domain.member.repository;

import java.util.Optional;
import org.clokey.member.entity.PendingFollow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingFollowRepository extends JpaRepository<PendingFollow, Long> {

    Optional<PendingFollow> findByFollowFrom_IdAndFollowTo_Id(Long fromMemberId, Long toMemberId);
}
