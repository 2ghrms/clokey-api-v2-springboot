package org.clokey.domain.member.repository;

import java.util.Optional;
import org.clokey.member.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowFrom_IdAndFollowTo_Id(Long fromMemberId, Long toMemberId);

    Optional<Follow> findByFollowFrom_IdAndFollowTo_Id(Long fromMemberId, Long toMemberId);
}
