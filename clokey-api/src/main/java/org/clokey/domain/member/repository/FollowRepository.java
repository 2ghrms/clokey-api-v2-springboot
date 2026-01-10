package org.clokey.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.clokey.member.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    boolean existsByFollowFrom_IdAndFollowTo_Id(Long fromMemberId, Long toMemberId);

    @Query(
            """
    SELECT f.followTo.id
    FROM Follow f
    WHERE f.followFrom.id = :fromMemberId
      AND f.followTo.id IN :toMemberIds
""")
    List<Long> findFollowedMemberIds(Long fromMemberId, List<Long> toMemberIds);

    Optional<Follow> findByFollowFrom_IdAndFollowTo_Id(Long fromMemberId, Long toMemberId);
}
